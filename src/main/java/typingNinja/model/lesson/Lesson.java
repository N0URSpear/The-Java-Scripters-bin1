package typingNinja.model.lesson;

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

    // Database row identifier.
    public int getLessonId() { return lessonId; }
    // Owning user for this lesson selection.
    public int getUserId() { return userId; }
    // Shorthand used to look up prompts or configure the controller.
    public String getLessonType() { return lessonType; }
    // Optional prompt provided for custom/AI lessons.
    public String getPrompt() { return prompt; }
    // Duration in minutes as originally requested.
    public int getDurationMinutes() { return durationMinutes; }
    // Flags for uppercase inclusion.
    public boolean isUpperCase() { return upperCase; }
    // Flags for digit inclusion.
    public boolean isNumbers() { return numbers; }
    // Flags for punctuation inclusion.
    public boolean isPunctuation() { return punctuation; }
    // Flags for special character inclusion.
    public boolean isSpecialChars() { return specialChars; }
}
