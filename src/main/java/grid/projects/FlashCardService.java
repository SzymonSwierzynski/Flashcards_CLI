package grid.projects;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class FlashCardService {

    private final List<FlashCard> flashcards = new ArrayList<>();


    public AddResult addCard(String term, String definition) {
        if (term == null || definition == null || term.isBlank() || definition.isBlank()) {
            return AddResult.NO_VALUE;
        }
        if (termExists(term)) {
            return AddResult.TERM_ALREADY_EXISTS;
        }
        if (definitionExists(definition)) {
            return AddResult.DEFINITION_ALREADY_EXISTS;
        }

        flashcards.add(new FlashCard(term, definition));
        return AddResult.ADDED;
    }

    public boolean removeCard(String term) {
        FlashCard card = findCardByTerm(term);
        if (card == null) {
            return false;
        }
        flashcards.remove(card);
        return true;
    }

    public boolean termExists(String term) {
        return findCardByTerm(term) != null;
    }


    public FlashCard findCardByTerm(String term) {
        if (term == null) {
            return null;
        }
        for (FlashCard card : flashcards) {
            if (normalizedComparison(term, card.getTerm())) {
                return card;
            }
        }
        return null;
    }

    public FlashCard findCardByDefinition(String definition) {
        if (definition == null) {
            return null;
        }
        for (FlashCard card : flashcards) {
            if (normalizedComparison(definition, card.getDefinition())) {
                return card;
            }
        }
        return null;
    }

    public void resetStats() {
        for (var card : flashcards) {
            card.resetMistakes();
        }
    }

    public boolean isEmpty() {
        return flashcards.isEmpty();
    }

    public int size() {
        return flashcards.size();
    }

    public void recordMistake(FlashCard card) {
        if (!termExists(card.getTerm())) {
            throw new IllegalArgumentException("No card like: " + card);
        }
        card.recordMistake();
    }

    List<FlashCard> findHardestCards() {
        List<FlashCard> hardestCards = new ArrayList<>();
        int maxMistakes = findMaximumMistakeNumber();
        for (var card : flashcards) {
            if (card.getMistakeCount() == maxMistakes) {
                hardestCards.add(card);
            }
        }
        if (flashcards.isEmpty() || maxMistakes == 0) {
            return List.of();
        }
        return List.copyOf(hardestCards);
    }


    int findMaximumMistakeNumber() {
        int maxMistakes = 0;
        for (FlashCard card : flashcards) {
            if (card.getMistakeCount() > maxMistakes) {
                maxMistakes = card.getMistakeCount();
            }
        }
        return maxMistakes;
    }

    boolean definitionExists(String definition) {
        for (FlashCard card : flashcards) {
            if (normalizedComparison(definition, card.getDefinition())) {
                return true;
            }
        }
        return false;
    }

    public void mergeImportedCards(List<FlashCard> importedCards) {
        for (FlashCard importedCard : importedCards) {
            FlashCard existingCard = findCardByTerm(importedCard.getTerm());
            if (existingCard != null) {
                existingCard.setDefinition(importedCard.getDefinition());
                existingCard.setMistakeCount(importedCard.getMistakeCount());
            } else {
                flashcards.add(
                        new FlashCard(
                                importedCard.getTerm(),
                                importedCard.getDefinition(),
                                importedCard.getMistakeCount()
                        )
                );
            }
        }
    }

    public List<FlashCard> getCards() {
        return List.copyOf(flashcards);
    }

    String normalizeForComparison(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFC).toLowerCase(Locale.ROOT);
    }

    public boolean normalizedComparison(String s1, String s2) {
        return normalizeForComparison(s1).equals(normalizeForComparison(s2));
    }

    public FlashCard getRandomCard() {
        if (flashcards.isEmpty()) {
            throw new IllegalStateException("No flashcards available");
        }
        int index = ThreadLocalRandom.current().nextInt(flashcards.size());
        return flashcards.get(index);
    }
}
