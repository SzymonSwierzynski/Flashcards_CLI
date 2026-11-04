package grid.projects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class LoggedConsoleTest {

    @TempDir
    Path tempDir;


    @Test
    void println_RecordsMessageInHistory() throws IOException {
        LoggedConsole console = new LoggedConsole(new Scanner(""));
        Path file = tempDir.resolve("log.txt");
        console.println("Welcome");
        console.saveLog(file);

        assertEquals(List.of("Welcome"), Files.readAllLines(file));
    }

    @Test
    void readLine_RecordsInputInHistory() throws IOException {
        LoggedConsole console = new LoggedConsole(new Scanner("Java\n"));
        Path file = tempDir.resolve("log.txt");

        assertEquals("Java", console.readLine());

        console.saveLog(file);

        assertEquals(List.of("Java"), Files.readAllLines(file));

    }

    @Test
    void readLine_afterSavingLog_updatesExistingLog() throws IOException {
        LoggedConsole console = new LoggedConsole(new Scanner("Java\n"));

        Path file = tempDir.resolve("log.txt");

        console.println("Start");
        console.saveLog(file);

        console.readLine();

        assertEquals(
                List.of("Start", "Java"),
                Files.readAllLines(file)
        );
    }
}