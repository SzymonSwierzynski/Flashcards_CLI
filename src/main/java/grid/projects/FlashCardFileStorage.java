package grid.projects;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FlashCardFileStorage {

    public List<FlashCard> load(Path path) throws IOException {

        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<FlashCard> cards = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (line.isBlank()) {
                continue;
            }

            String[] parts = line.split(":", 3);

            if (parts.length < 2) {
                throw new IOException("Invalid flashcard record");
            }
            int mistakes = 0;


            if (parts.length == 3) {

                try {
                    mistakes = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid mistake count");
                }
            }
            cards.add(new FlashCard(parts[0], parts[1], mistakes));
        }
        return cards;
    }

    public void save(Path path, List<FlashCard> cards) throws IOException {
        List<String> lines = new ArrayList<>();

        for (FlashCard card : cards) {
            lines.add(card.getTerm() + ":" + card.getDefinition() + ":" + card.getMistakeCount());
        }

        Files.write(path, lines, StandardCharsets.UTF_8);
    }
}
