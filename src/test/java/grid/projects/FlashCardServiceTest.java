package grid.projects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlashCardServiceTest {

    FlashCardService service;

    @BeforeEach
    void setUp() {
        service = new FlashCardService();
    }

    @Test
    void addCard_toEmptyList_returnsAdded() {
        // given
        String term = "France";
        String definition = "Paris";

        // when
        AddResult result = service.addCard(term, definition);

        // then
        assertEquals(AddResult.ADDED, result);
        assertEquals(1, service.size());
        assertTrue(service.termExists(term));
    }

    @Test
    void addCard_whenCardAlreadyExists_returnsTermAlreadyExists() {
        service.addCard("France", "Paris");
        String term = "France";
        String definition = "Paris";

        AddResult result = service.addCard(term, definition);

        assertEquals(AddResult.TERM_ALREADY_EXISTS, result);
        assertEquals(1, service.size());
        assertTrue(service.termExists(term));
    }

    @Test
    void addCard_whenCardAlreadyExists_returnsDefinitionAlreadyExists() {
        service.addCard("France", "Paris");
        String term = "Europe";
        String definition = "Paris";

        AddResult result = service.addCard(term, definition);

        assertEquals(AddResult.DEFINITION_ALREADY_EXISTS, result);
    }

    @Test
    void addCard_whenTermAlreadyExists_returnTERM_ALREADY_EXISTS() {
        service.addCard("France", "Paris");
        String term = "France";
        String definition = "Paris";

        AddResult result = service.addCard(term, definition);

        assertEquals(AddResult.TERM_ALREADY_EXISTS, result);
        assertEquals(1, service.size());
        assertTrue(service.termExists(term));
    }

    @Test
    void addCard_withNullValues() {
        String term = null;
        String definition = null;

        AddResult result = service.addCard(term, definition);

        assertEquals(AddResult.NO_VALUE, result);
        assertEquals(0, service.size());
        assertFalse(service.termExists(term));
    }

    @Test
    void addCard_withBlankValues() {
        String term = "";
        String definition = "";

        AddResult result = service.addCard(term, definition);

        assertEquals(AddResult.NO_VALUE, result);
        assertEquals(0, service.size());
        assertFalse(service.termExists(term));
    }


    @Test
    void removeCard_ValidArgumentCardExists_returnsTrue() {
        service.addCard("France", "Paris");
        String term = "France";

        boolean result = service.removeCard(term);

        assertTrue(result);
        assertEquals(0, service.size());
        assertFalse(service.termExists(term));
    }

    @Test
    void removeCard_ValidArgumentCardDoesntExist_returnsFalse() {
        service.addCard("Europe", "Paris");
        String term = "FRANCE";

        boolean result = service.removeCard(term);

        assertFalse(result);
        assertEquals(1, service.size());
        assertFalse(service.termExists(term));
        assertTrue(service.termExists("europe"));
    }

    @Test
    void removeCard_ValidArgumentEmptyList_returnsFalse() {
        String term = "France";

        boolean result = service.removeCard(term);

        assertFalse(result);
        assertEquals(0, service.size());
        assertFalse(service.termExists(term));
    }

    @Test
    void removeCard_NullArgument_returnsFalse() {
        service.addCard("France", "Paris");
        String term = null;

        boolean result = service.removeCard(term);

        assertFalse(result);
        assertEquals(1, service.size());
        assertFalse(service.termExists(term));
    }

    @Test
    void removeCard_BlankArgument_returnsFalse() {
        service.addCard("France", "Paris");
        String term = "";

        boolean result = service.removeCard(term);

        assertFalse(result);
        assertEquals(1, service.size());
        assertFalse(service.termExists(term));
    }

    @Test
    void termExists_returnsTrue() {
        String term = "France";
        service.addCard("France", "Paris");

        assertTrue(service.termExists(term));
    }

    @Test
    void termExists_returnsFalse() {
        String term = "Poland";
        service.addCard("France", "Paris");

        assertFalse(service.termExists(term));
    }

    @Test
    void definitionExists_returnsTrue() {
        String definition = "Paris";
        service.addCard("France", "Paris");

        assertTrue(service.definitionExists(definition));
    }

    @Test
    void definitionExists_returnsFalse() {
        String definition = "Warsaw";
        service.addCard("France", "Paris");

        assertFalse(service.definitionExists(definition));
    }

    @Test
    void findCardByTerm() {
        service.addCard("France", "Paris");
        String term = "France";

        FlashCard card = service.findCardByTerm(term);

        assertEquals("France", card.getTerm());
        assertEquals("Paris", card.getDefinition());
    }

    @Test
    void findCardByTerm_BlankTerm() {
        service.addCard("France", "Paris");
        String term = "";

        FlashCard card = service.findCardByTerm(term);

        assertNull(card);
    }

    @Test
    void findCardByDefinition() {
        service.addCard("France", "Paris");
        String definition = "Paris";

        FlashCard card = service.findCardByDefinition(definition);

        assertEquals("France", card.getTerm());
        assertEquals("Paris", card.getDefinition());
    }

    @Test
    void findCardByDefinition_BlankDefinition() {
        service.addCard("France", "Paris");
        String definition = "";

        FlashCard card = service.findCardByDefinition(definition);

        assertNull(card);
    }

    @Test
    void findCardByDefinition_NullDefinition() {
        service.addCard("France", "Paris");
        String definition = null;

        FlashCard card = service.findCardByDefinition(definition);

        assertNull(card);
    }

    @Test
    void resetStats() {
        service.addCard("France", "Paris");
        FlashCard card = service.findCardByTerm("France");
        service.recordMistake(card);

        service.resetStats();

        assertEquals(0, card.getMistakeCount());
    }

    @Test
    void recordMistake() {
        service.addCard("France", "Paris");
        FlashCard card = service.findCardByTerm("France");

        service.recordMistake(card);

        assertEquals(1, card.getMistakeCount());
    }

    @Test
    void recordMistake_CardDoesntExist() {
        FlashCard card = new FlashCard("France", "Paris");
        assertThrows(IllegalArgumentException.class, () -> service.recordMistake(card));
    }

    @Test
    void findHardestCards_OneHardestCard() {
        service.addCard("Poland", "Warsaw");
        service.addCard("France", "Paris");
        service.addCard("Germany", "Berlin");

        FlashCard card1 = service.findCardByTerm("Poland");
        FlashCard card2 = service.findCardByTerm("France");
        FlashCard card3 = service.findCardByTerm("Germany");

        service.recordMistake(card1);
        service.recordMistake(card1);

        service.recordMistake(card2);
        service.recordMistake(card3);

        List<FlashCard> hardestCards = service.findHardestCards();
        assertEquals(card1, hardestCards.get(0));
        assertEquals(1, hardestCards.size());
    }

    @Test
    void findHardestCards_TieBetweenCards() {
        service.addCard("Poland", "Warsaw");
        service.addCard("France", "Paris");
        service.addCard("Germany", "Berlin");

        FlashCard card1 = service.findCardByTerm("Poland");
        FlashCard card2 = service.findCardByTerm("France");
        FlashCard card3 = service.findCardByTerm("Germany");

        service.recordMistake(card1);
        service.recordMistake(card2);
        service.recordMistake(card3);

        List<FlashCard> hardestCards = service.findHardestCards();

        assertEquals(3, hardestCards.size());
        assertEquals(1, hardestCards.get(0).getMistakeCount());
        assertEquals(1, hardestCards.get(1).getMistakeCount());
        assertEquals(1, hardestCards.get(2).getMistakeCount());
    }

    @Test
    void findHardestCards_ZeroMistakes() {
        service.addCard("Poland", "Warsaw");
        service.addCard("France", "Paris");
        service.addCard("Germany", "Berlin");

        List<FlashCard> hardestCards = service.findHardestCards();

        assertEquals(0, hardestCards.size());
    }

    @Test
    void findMaximumMistakeNumber() {
        service.addCard("Poland", "Warsaw");
        service.addCard("France", "Paris");
        service.addCard("Germany", "Berlin");

        FlashCard card1 = service.findCardByTerm("Poland");
        FlashCard card2 = service.findCardByTerm("France");
        FlashCard card3 = service.findCardByTerm("Germany");

        service.recordMistake(card1);
        service.recordMistake(card1);

        service.recordMistake(card2);
        service.recordMistake(card3);

        int maxMistakeNumber = service.findMaximumMistakeNumber();

        assertEquals(card1.getMistakeCount(), maxMistakeNumber);
    }

    @Test
    void mergeImportedCards_NoExistingCards() {
        List<FlashCard> importedCards = List.of(
                new FlashCard("France", "Paris", 2),
                new FlashCard("Poland", "Warsaw", 3)
        );

        service.mergeImportedCards(importedCards);

        assertEquals(2, service.size());

        FlashCard france = service.findCardByTerm("France");
        assertNotNull(france);
        assertEquals("Paris", france.getDefinition());
        assertEquals(2, france.getMistakeCount());

        FlashCard poland = service.findCardByTerm("Poland");
        assertNotNull(poland);
        assertEquals("Warsaw", poland.getDefinition());
        assertEquals(3, poland.getMistakeCount());
    }

    @Test
    void mergeImportedCards_UpdatesExistingCard() {
        service.addCard("France", "Nicea");

        List<FlashCard> importedCards = List.of(
                new FlashCard("France", "Paris", 2),
                new FlashCard("Poland", "Warsaw", 1)
        );

        service.mergeImportedCards(importedCards);

        assertEquals(2, service.size());
        assertEquals("Paris", service.findCardByTerm("France").getDefinition());
        assertEquals(2, service.findCardByTerm("France").getMistakeCount());
        assertNotNull(service.findCardByTerm("Poland"));
    }

    @Test
    void isEmpty() {
        assertTrue(service.isEmpty());
    }

    @Test
    void getRandomCard_EmptyList_Throws() {
        assertThrows(IllegalStateException.class, () -> service.getRandomCard());
    }

    @Test
    void getRandomCard_returnsRandomCard() {
        service.addCard("France", "Paris");

        assertEquals("France", service.getRandomCard().getTerm());
    }
}