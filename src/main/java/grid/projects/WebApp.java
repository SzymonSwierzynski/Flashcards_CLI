package grid.projects;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Browser UI for the flashcards. Usage: WebApp [cards.txt] [port].
 * Loads the file on start (if it exists) and saves to it after every change.
 */
public class WebApp {

    private static final int DEFAULT_PORT = 8080;
    private static final int MAX_BODY_BYTES = 5 * 1024 * 1024;
    private static final String JSON = "application/json; charset=utf-8";

    private final FlashCardService service;
    private final FlashCardFileStorage storage;
    private final Path dataFile;

    public WebApp(FlashCardService service, FlashCardFileStorage storage, Path dataFile) {
        this.service = service;
        this.storage = storage;
        this.dataFile = dataFile;
    }

    public static void main(String[] args) throws IOException {
        Path dataFile = args.length > 0 ? Path.of(args[0]) : null;
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;
        WebApp app = new WebApp(new FlashCardService(), new FlashCardFileStorage(), dataFile);
        if (dataFile != null && Files.exists(dataFile)) {
            app.service.mergeImportedCards(app.storage.load(dataFile));
        }
        app.start(port);
        System.out.println("FlashCards UI running at http://localhost:" + port);
    }

    HttpServer start(int port) throws IOException {
        // Loopback only: the API can write files and has no auth.
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", route(this::index));
        server.createContext("/api/cards", route(this::cards));
        server.createContext("/api/remove", route(this::remove));
        server.createContext("/api/next", route(this::next));
        server.createContext("/api/answer", route(this::answer));
        server.createContext("/api/reset", route(this::reset));
        server.createContext("/api/import", route(this::importCards));
        server.createContext("/api/export", route(this::exportCards));
        server.start();
        return server;
    }

    private HttpHandler route(HttpHandler handler) {
        return exchange -> {
            try (exchange) {
                handler.handle(exchange);
            } catch (IOException | RuntimeException e) {
                send(exchange, 500, JSON, "{\"error\":" + quote(String.valueOf(e.getMessage())) + "}");
            }
        };
    }

    private void index(HttpExchange exchange) throws IOException {
        try (InputStream page = WebApp.class.getResourceAsStream("/web/index.html")) {
            if (page == null || !"/".equals(exchange.getRequestURI().getPath())) {
                send(exchange, 404, "text/plain", "Not found");
                return;
            }
            send(exchange, 200, "text/html; charset=utf-8", new String(page.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private void cards(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            Map<String, String> form = readForm(exchange);
            AddResult result = service.addCard(form.get("term"), form.get("definition"));
            persist();
            send(exchange, result == AddResult.ADDED ? 200 : 409, JSON, "{\"result\":\"" + result + "\"}");
            return;
        }
        StringBuilder json = new StringBuilder("[");
        List<FlashCard> all = service.getCards();
        int max = service.findMaximumMistakeNumber();
        for (FlashCard card : all) {
            if (json.length() > 1) {
                json.append(',');
            }
            json.append("{\"term\":").append(quote(card.getTerm()))
                    .append(",\"definition\":").append(quote(card.getDefinition()))
                    .append(",\"mistakes\":").append(card.getMistakeCount())
                    .append(",\"hardest\":").append(max > 0 && card.getMistakeCount() == max)
                    .append('}');
        }
        send(exchange, 200, JSON, json.append(']').toString());
    }

    private void remove(HttpExchange exchange) throws IOException {
        boolean removed = service.removeCard(readForm(exchange).get("term"));
        persist();
        send(exchange, removed ? 200 : 404, JSON, "{\"removed\":" + removed + "}");
    }

    private void next(HttpExchange exchange) throws IOException {
        if (service.isEmpty()) {
            send(exchange, 404, JSON, "{\"term\":null}");
            return;
        }
        send(exchange, 200, JSON, "{\"term\":" + quote(service.getRandomCard().getTerm()) + "}");
    }

    private void answer(HttpExchange exchange) throws IOException {
        Map<String, String> form = readForm(exchange);
        FlashCard card = service.findCardByTerm(form.get("term"));
        if (card == null) {
            send(exchange, 404, JSON, "{\"error\":\"Card not found\"}");
            return;
        }
        String answer = form.getOrDefault("answer", "");
        boolean correct = service.normalizedComparison(answer, card.getDefinition());
        String otherTerm = null;
        if (!correct) {
            service.recordMistake(card);
            FlashCard other = service.findCardByDefinition(answer);
            otherTerm = other == null ? null : other.getTerm();
            persist();
        }
        send(exchange, 200, JSON, "{\"correct\":" + correct
                + ",\"definition\":" + quote(card.getDefinition())
                + ",\"otherTerm\":" + (otherTerm == null ? "null" : quote(otherTerm)) + "}");
    }

    private void reset(HttpExchange exchange) throws IOException {
        service.resetStats();
        persist();
        send(exchange, 200, JSON, "{}");
    }

    private void importCards(HttpExchange exchange) throws IOException {
        Path temp = Files.createTempFile("flashcards", ".txt");
        try {
            Files.write(temp, readBody(exchange));
            List<FlashCard> imported = storage.load(temp);
            service.mergeImportedCards(imported);
            persist();
            send(exchange, 200, JSON, "{\"loaded\":" + imported.size() + "}");
        } catch (IOException e) {
            send(exchange, 400, JSON, "{\"error\":" + quote(e.getMessage()) + "}");
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private void exportCards(HttpExchange exchange) throws IOException {
        Path temp = Files.createTempFile("flashcards", ".txt");
        try {
            storage.save(temp, service.getCards());
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"flashcards.txt\"");
            send(exchange, 200, "text/plain; charset=utf-8", Files.readString(temp, StandardCharsets.UTF_8));
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private void persist() throws IOException {
        if (dataFile != null) {
            storage.save(dataFile, service.getCards());
        }
    }

    private static byte[] readBody(HttpExchange exchange) throws IOException {
        byte[] body = exchange.getRequestBody().readNBytes(MAX_BODY_BYTES + 1);
        if (body.length > MAX_BODY_BYTES) {
            throw new IOException("Request too large");
        }
        return body;
    }

    private static Map<String, String> readForm(HttpExchange exchange) throws IOException {
        Map<String, String> form = new HashMap<>();
        for (String pair : new String(readBody(exchange), StandardCharsets.UTF_8).split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                form.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return form;
    }

    private static void send(HttpExchange exchange, int status, String type, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    static String quote(String value) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                default -> {
                    if (c < ' ') {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }
}
