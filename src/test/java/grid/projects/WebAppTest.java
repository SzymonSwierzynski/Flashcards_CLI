package grid.projects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class WebAppTest {

    @Test
    void quoteEscapesJsonSpecialCharacters() {
        assertEquals("\"a\\\"b\\\\c\\u000a\"", WebApp.quote("a\"b\\c\n"));
    }

    @Test
    void addThenAnswerWrongRecordsMistake() throws IOException, InterruptedException {
        FlashCardService service = new FlashCardService();
        HttpServer server = new WebApp(service, new FlashCardFileStorage(), null).start(0);
        String base = "http://127.0.0.1:" + server.getAddress().getPort();
        HttpClient client = HttpClient.newHttpClient();
        try {
            assertEquals(200, post(client, base + "/api/cards", "term=France&definition=Paris").statusCode());
            assertEquals(409, post(client, base + "/api/cards", "term=france&definition=Lyon").statusCode());
            String body = post(client, base + "/api/answer", "term=France&answer=Rome").body();
            assertTrue(body.contains("\"correct\":false"));
            assertEquals(1, service.findCardByTerm("France").getMistakeCount());
        } finally {
            server.stop(0);
        }
    }

    private HttpResponse<String> post(HttpClient client, String url, String form)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(form)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
