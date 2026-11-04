package grid.projects;

public class FlashCard {

    private final String term;
    private String definition;
    private int mistakeCount;

    public FlashCard(String term, String definition) {
        this.term = term;
        this.definition = definition;
        this.mistakeCount = 0;
    }

    public FlashCard(String term, String definition, int mistakeCount) {
        this.term = term;
        this.definition = definition;
        this.mistakeCount = mistakeCount;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public void setMistakeCount(int mistakeCount) {
        this.mistakeCount = mistakeCount;
    }

    public int getMistakeCount() {
        return mistakeCount;
    }

    public String getDefinition() {
        return definition;
    }

    public String getTerm() {
        return term;
    }


    void recordMistake() {
        mistakeCount++;
    }

    void resetMistakes() {
        mistakeCount = 0;
    }

    @Override
    public String toString() {
        return term + ":" + definition + ":" + mistakeCount;
    }
}
