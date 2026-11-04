package grid.projects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private FlashCardService service;
    private Main main;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new FlashCardService();
        LoggedConsole console = new LoggedConsole(new Scanner(""));
        main = new Main(console, service, new FlashCardFileStorage());
    }

    @Test
    void parseArguments_bothFlagsProvided_returnsBoth() {
        String[] args = new String[] { "-import", "input.txt", "-export", "output.txt" };
        var result = main.parseArguments(args);

        assertEquals("input.txt", result.get("-import"));
        assertEquals("output.txt", result.get("-export"));
    }

    @Test
    void parseArguments_noFlagsProvided_returnsEmptyMap() {
        String[] args = new String[] {};
        assertTrue(main.parseArguments(args).isEmpty());
    }

    @Test
    void parseArguments_noFilePathsProvided_returnsEmptyMap() {
        String[] args = new String[] { "-import", "-export" };
        assertTrue(main.parseArguments(args).isEmpty());
    }

    private Main createMain(String input) {
        LoggedConsole console = new LoggedConsole(new Scanner(input));
        return new Main(console, service, new FlashCardFileStorage());
    }

    @Test
    void addCard_validInput_addsCard() {
        main = createMain("France\nParis\n");
        main.addCard();

        assertEquals(1, service.size());

        FlashCard card = service.findCardByTerm("France");
        assertNotNull(card);
        assertEquals("Paris", card.getDefinition());
    }

    @Test
    void addCard_whenTermIsEmpty_doesNotAddCard() {
        main = createMain("\n");

        main.addCard();

        assertTrue(service.isEmpty());
    }

    @Test
    void askAboutCard_whenAnswerIsCorrect_doesNotRecordMistake() {
        service.addCard("France", "Paris");
        main = createMain("1\nParis\n");

        main.askAboutCard();

        assertEquals(0, service.findCardByTerm("France").getMistakeCount());
    }

    @Test
    void askAboutCard_whenAnswerIsWrong_recordsMistake() {
        service.addCard("France", "Paris");
        main = createMain("1\nLondon\n");

        main.askAboutCard();

        assertEquals(1, service.findCardByTerm("France").getMistakeCount());
    }

    @Test
    void askAboutCard_whenServiceIsEmpty_doesNotAskForInput() {
        main = createMain("");

        assertDoesNotThrow(() -> main.askAboutCard());
        assertTrue(service.isEmpty());
    }

    @Test
    void showHardestCards_whenNoMistakes_printsMessage() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(output));

            service.addCard("France", "Paris");
            main.showHardestCards();

            assertTrue(output.toString().contains(
                    "There are no cards with errors."
            ));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void run_whenExitIsEntered_terminatesApplication() throws IOException {
        main = createMain("exit\n");

        assertDoesNotThrow(() -> main.run(Map.of()));
    }

    @Test
    void removeCard_whenCardExists_removesCard() {
        service.addCard("France", "Paris");
        main = createMain("France\n");

        main.removeCard();

        assertFalse(service.termExists("France"));
        assertEquals(0, service.size());
    }

    @Test
    void importCards_whenFileIsValid_importsCards() throws IOException {
        Path file = tempDir.resolve("import.txt");
        Files.writeString(file, "France:Paris:2\n");

        main = createMain(file.toString() + "\n");

        main.importCards();

        assertEquals(1, service.size());
        assertEquals("Paris", service.findCardByTerm("France").getDefinition());
        assertEquals(2, service.findCardByTerm("France").getMistakeCount());
    }

    @Test
    void loadCards_whenFileDoesNotExist_doesNotThrow() {
        Path file = tempDir.resolve("missing.txt");

        assertDoesNotThrow(() -> main.loadCards(file));
        assertTrue(service.isEmpty());
    }

    @Test
    void exportCards_whenFilenameIsValid_exportsCards() throws IOException {
        service.addCard("France", "Paris");
        Path file = tempDir.resolve("export.txt");

        main = createMain(file + "\n");

        main.exportCards();

        assertEquals(List.of("France:Paris:0"), Files.readAllLines(file));
    }

    @Test
    void saveCards_whenPathIsValid_savesCards() throws IOException {
        service.addCard("France", "Paris");
        Path file = tempDir.resolve("cards.txt");

        main.saveCards(file);

        assertEquals(List.of("France:Paris:0"), Files.readAllLines(file));
    }

    @Test
    void logCards_whenFilenameIsValid_createsLogFile()
            throws IOException {
        Path file = tempDir.resolve("log.txt");
        main = createMain(file.toString() + "\n");

        main.logCards();

        assertTrue(Files.exists(file));

        List<String> lines = Files.readAllLines(file);
        assertTrue(lines.contains("File name:"));
        assertTrue(lines.contains("The log has been saved"));
    }
}