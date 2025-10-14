package typingNinja.model.lesson;

import java.util.List;

public class CustomPrompts {
  public record Prompt(String title, String text, int durationSeconds) {}
  private final List<Prompt> prompts = List.of(
    new Prompt("Wolves to Companions", String.join(" ",
      "Dogs are among the most familiar animals in human history, and",
      "their story begins with wolves. Thousands of years ago,",
      "certain wolves began to live closer to people, scavenging near",
      "campsites. Over time, a mutual relationship formed. Humans provided"
    ), 60)
  );
  private int idx = 0;
  public Prompt current() { return prompts.get(idx); }
}
