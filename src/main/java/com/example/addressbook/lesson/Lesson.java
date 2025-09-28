package com.example.addressbook.lesson;

public class Lesson {
    private final int lessonId;
    private final int userId;
    private final String lessonType;   // "1a"…"4f", "CustomTopic", "FreeWeakKeys", "FreeAnything"
    private final String prompt;       // only used when lessonType == CustomTopic (<=50 chars per DB)
    private final int durationMinutes; // from LessonDuration (minutes)
    private final boolean upperCase;
    private final boolean numbers;
    private final boolean punctuation;
    private final boolean specialChars;

    public Lesson(int lessonId, int userId, String lessonType, String prompt,
                  int durationMinutes, boolean upperCase, boolean numbers,
                  boolean punctuation, boolean specialChars) {
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

    public int getLessonId() { return lessonId; }
    public int getUserId() { return userId; }
    public String getLessonType() { return lessonType; }
    public String getPrompt() { return prompt; }
    public int getDurationMinutes() { return durationMinutes; }
    public boolean isUpperCase() { return upperCase; }
    public boolean isNumbers() { return numbers; }
    public boolean isPunctuation() { return punctuation; }
    public boolean isSpecialChars() { return specialChars; }
}
