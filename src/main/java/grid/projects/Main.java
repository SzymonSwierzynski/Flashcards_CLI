package grid.projects;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

public class Main {

    private static final String IMPORT = "-import";
    private static final String EXPORT = "-export";
    private final LoggedConsole console;
    private final FlashCardService service;
    private final FlashCardFileStorage storage;

    public Main() {
        this.console = new LoggedConsole(new Scanner(System.in, StandardCharsets.UTF_8));
        this.service = new FlashCardService();
        this.storage = new FlashCardFileStorage();
    }

    public Main(LoggedConsole console, FlashCardService service, FlashCardFileStorage storage) {
        this.console = console;
        this.service = service;
        this.storage = storage;
    }

    public static void main(String[] args) throws IOException {
        Main app = new Main();
        Map<String, String> parsedArgs = app.parseArguments(args);
        app.run(parsedArgs);
    }

    void run(Map<String, String> parsedArgs) throws IOException {

        String importFile = parsedArgs.get(IMPORT);
        if (importFile != null) {
            loadCards(Path.of(importFile));
        }

        boolean isActive = true;

        while (isActive) {
            console.println("Input the action (add, remove, import, export,"
                    + " ask, exit, log, hardest card, reset stats):");
            String action = console.readLine().toLowerCase(Locale.ROOT);
            switch (action) {
                case "add" -> addCard();
                case "remove" -> removeCard();
                case "ask" -> askAboutCard();
                case "import" -> importCards();
                case "export" -> exportCards();
                case "log" -> logCards();
                case "hardest card" -> showHardestCards();
                case "reset stats" -> resetStats();
                case "exit" -> {
                    String exportFile = parsedArgs.get(EXPORT);
                    if (exportFile != null) {
                        saveCards(Path.of(exportFile));
                    }
                    console.println("Bye bye!");
                    isActive = false;
                }
                default -> console.println("Unknown action");
            }
        }
    }


    void addCard() {
        console.println("The card:");
        String term = console.readLine();

        if (term.isEmpty()) {
            console.println("Term cannot be empty");
            return;
        }

        console.println("The definition of the card:");
        String definition = console.readLine();

        if (definition.isEmpty()) {
            console.println("Definition cannot be empty");
            return;
        }

        AddResult result = service.addCard(term, definition);

        switch (result) {
            case ADDED -> console.println("The pair (\"" + term + "\":\"" + definition + "\") has been added");
            case TERM_ALREADY_EXISTS -> console.println("The card \"" + term + "\" already exists.");
            case DEFINITION_ALREADY_EXISTS -> console.println("The definition \"" + definition + "\" already exists.");
        }

    }

    void removeCard() {
        console.println("Which card?");
        String termToDelete = console.readLine();
        if (service.removeCard(termToDelete)) {
            console.println("The card: " + termToDelete + " has been removed");
        } else {
            console.println("There is no card like: " + termToDelete);
        }
    }

    void askAboutCard() {
        if (service.isEmpty()) {
            console.println("No flashcards to practice on");
            return;
        }
        console.println("How many times to ask?");
        String response = console.readLine();
        if (response == null || response.isBlank()) {
            return;
        }
        int numberOfCardsToAsk = Integer.parseInt(response);
        for (int i = 0; i < numberOfCardsToAsk; i++) {
            FlashCard randomCard = service.getRandomCard();
            console.println("Print the definition of " + "\"" + randomCard.getTerm() + "\":");
            String answer = console.readLine();
            if (service.normalizedComparison(answer, randomCard.getDefinition())) {
                console.println("Correct!");
            } else {
                randomCard.recordMistake();
                FlashCard matchingCardToAnswer = service.findCardByDefinition(answer);

                if (matchingCardToAnswer != null) {
                    console.println("Wrong. The right answer is \"" + randomCard.getDefinition()
                            + "\", but your definition is correct for \""
                            + matchingCardToAnswer.getTerm() + "\".");
                } else {
                    console.println("Wrong. The right answer is " + "\"" + randomCard.getDefinition() + "\".");
                }
            }
        }
    }

    void importCards() {
        console.println("File name:");
        String filename = console.readLine();
        if (filename.isBlank()) {
            console.println("File name cannot be empty");
            return;
        }

        loadCards(Path.of(filename));
    }

    void loadCards(Path path) {
        try {
            List<FlashCard> importedCards = storage.load(path);
            service.mergeImportedCards(importedCards);
            console.println(importedCards.size() + " cards have been loaded");
        } catch (IOException e) {
            console.println("Could not load cards: " + e.getMessage());
        }
    }

    void exportCards() throws IOException {
        console.println("File name:");
        String filename = console.readLine();
        if (filename == null || filename.isBlank()) {
            console.println("Name of export file must not be empty");
            return;
        }
        saveCards(Path.of(filename));
    }

    void saveCards(Path path) {
        int numberOfCards = service.size();

        try {
            storage.save(path, service.getCards());

            console.println(numberOfCards + " have been saved");
        } catch (IOException e) {
            console.println("Could not save cards: " + e.getMessage());
        }
    }

    void logCards() throws IOException {
        console.println("File name:");
        String filename = console.readLine();
        if (filename.isBlank()) {
            console.println("File name cannot be empty");
            return;
        }

        try {
            console.saveLog(Path.of(filename));
            console.println("The log has been saved");
        } catch (IOException e) {
            console.println("Could not save the log: " + e.getMessage());
        }
    }

    void showHardestCards() {
        List<FlashCard> hardestCards = service.findHardestCards();
        if (hardestCards.isEmpty() || hardestCards.get(0).getMistakeCount() == 0) {
            console.println("There are no cards with errors.");
            return;
        }

        int maxMistakes = hardestCards.get(0).getMistakeCount();

        if (hardestCards.size() > 1) {
            String output = "The hardest cards are ";
            for (FlashCard card : hardestCards) {
                output = String.join(output, "\"" + card.getTerm() + "\" ");
            }
            output = String.join(output, "You have " + maxMistakes + " errors answering them");
            console.println(output);
        } else {
            console.println("The hardest card is " + "\"" + hardestCards.get(0).getTerm()
                    + "\". You have " + maxMistakes + " errors answering it.");
        }
    }

    void resetStats() {
        service.resetStats();
        console.println("Card statistics have been reset.");
    }

    Map<String, String> parseArguments(String[] args) {
        Map<String, String> parsedArgs = new HashMap<>();
        int MIN_ARGS_NUMBER = 2;
        if (args.length < MIN_ARGS_NUMBER) {
            return parsedArgs;
        }
        for (int i = 0; i < args.length - 1; i++) {
            if (service.normalizedComparison(args[i], IMPORT) && !service.normalizedComparison(args[i + 1], EXPORT)) {
                parsedArgs.put(IMPORT, args[i + 1]);
            }
            if (service.normalizedComparison(args[i], EXPORT)
                    && !service.normalizedComparison(args[i + 1], IMPORT)) {
                parsedArgs.put(EXPORT, args[i + 1]);
            }
        }
        return parsedArgs;
    }
}

