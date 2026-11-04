package grid.projects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlashCardFileStorageTest {

    @TempDir
    Path tempDir;

    private final FlashCardFileStorage storage = new FlashCardFileStorage();


    @Test
    void load_validRecords_returnsCardsTest() throws IOException {
        Path file = tempDir.resolve("flashcards.txt");

        Files.write(file, List.of(
                "France:Paris:2",
                "Poland:Warsaw:0"
        ));

        List<FlashCard> cards = storage.load(file);

        assertEquals(2, cards.size());
        assertEquals("France", cards.get(0).getTerm());
        assertEquals("Paris", cards.get(0).getDefinition());
        assertEquals(2, cards.get(0).getMistakeCount());

        assertEquals("Poland", cards.get(1).getTerm());
        assertEquals("Warsaw", cards.get(1).getDefinition());
        assertEquals(0, cards.get(1).getMistakeCount());
    }

    @Test
    void load_whenRecordHasNoDefinition_throws() throws IOException {
        Path file = tempDir.resolve("invalid.txt");
        Files.writeString(file, "France");

        assertThrows(IOException.class, () -> storage.load(file));
    }

    @Test
    void load_whenMistakeCountIsInvalid_throws()
            throws IOException {
        Path file = tempDir.resolve("invalid.txt");
        Files.writeString(file, "France:Paris:abc");

        assertThrows(IOException.class, () -> storage.load(file));
    }

    @Test
    void save() throws IOException {
        Path file = tempDir.resolve("flashcards.txt");

        List<FlashCard> cards  = List.of(
                new FlashCard("France", "Paris", 2),
                new FlashCard("Poland", "Warsaw", 0)
        );

        storage.save(file, cards);

        List<String> lines = Files.readAllLines(file);

        assertEquals(List.of(
                "France:Paris:2",
                "Poland:Warsaw:0"
        ), lines);
    }
}