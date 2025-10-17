package typingNinja.model.lesson;

/**
 * Immutable representation of a lesson selection persisted in the database.
 */
public class Lesson {
    private final int lessonId;
    private final int userId;
    private final String lessonType;
    private final String prompt;
    private final int durationMinutes;
    private final boolean upperCase;
    private final boolean numbers;
    private final boolean punctuation;
    private final boolean specialChars;

    /**
     * Constructs a new lesson DTO.
     */
    public Lesson(int lessonId, int userId, String lessonType, String prompt,
                  int durationMinutes, boolean upperCase, boolean numbers,
                  boolean punctuation, boolean specialChars) {
        // Immutable record-style holder for whatever lesson the user just queued up.
        this.lessonId = lessonId;
        this.userId = userId;
        this.lessonType = lessonType;
        this.prompt = prompt;
        this.durationMinutes = durationMinutes;
        this.upperCase = upperCase;
        this.numbers = numbers;
        this.punctuation = punctuation;
        this.specialChars = specialChars;
    }

    /** @return database row identifier */
    public int getLessonId() { return lessonId; }
    /** @return owning user for this lesson selection */
    public int getUserId() { return userId; }
    /** @return shorthand lesson type string */
    public String getLessonType() { return lessonType; }
    /** @return optional prompt text for custom lessons */
    public String getPrompt() { return prompt; }
    /** @return requested duration in minutes */
    public int getDurationMinutes() { return durationMinutes; }
    /** @return whether uppercase characters were requested */
    public boolean isUpperCase() { return upperCase; }
    /** @return whether digits were requested */
    public boolean isNumbers() { return numbers; }
    /** @return whether punctuation was requested */
    public boolean isPunctuation() { return punctuation; }
    /** @return whether special characters were requested */
    public boolean isSpecialChars() { return specialChars; }
}
