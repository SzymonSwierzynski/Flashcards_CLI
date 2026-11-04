package grid.projects;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LoggedConsole {

    private final Scanner scanner;
    private final List<String> history = new ArrayList<>();
    private Path logPath;

    public LoggedConsole(Scanner scanner) {
        this.scanner = scanner;
    }

    public void println(String message) {
        System.out.println(message);
        history.add(message);
        updateLogIfEnabled();
    }

    public String readLine() {
        String input = scanner.nextLine();
        history.add(input);
        updateLogIfEnabled();
        return input;
    }

    public void saveLog(Path path) throws IOException {
        Files.write(path, history, StandardCharsets.UTF_8);
        logPath = path;
    }

    public void updateLogIfEnabled() {
        if (logPath == null) {
            return;
        }
        try {
            Files.write(logPath, history, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
