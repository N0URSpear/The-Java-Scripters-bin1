package typingNinja.model.ai;

import java.util.*;

/**
 * Generates deterministic pseudo-random passages without hitting an external AI.
 */
public class LocalSimpleTextService implements AITextService {

    @Override
    public String generatePassage(String topic, int targetWords,
                                  boolean includeUpper, boolean includeNumbers,
                                  boolean includePunct, boolean includeSpecial) {
        // Deterministic pseudo-random generator so identical prompts yield stable passages.

        long seed = (topic == null ? 0 : topic.toLowerCase(Locale.ROOT).hashCode());
        Random r = new Random(seed);

        if (targetWords < 60) targetWords = 60;
        if (targetWords > 400) targetWords = 400;

        String[] common = ("the a an this that these those every some many few simple little great " +
                "clear quick slow bright soft careful steady common daily modern classic").split(" ");
        String[] verbs = ("is are seems feels makes shows brings gives builds improves explains helps " +
                "supports creates discovers explores compares measures connects").split(" ");
        String[] nouns = ("idea method habit exercise topic lesson passage keyboard finger hand screen " +
                "student practice result time number letter symbol example story step line").split(" ");
        String[] punct = {".", ".", ".", ".", ",", ";", ".", "!", ".", "."};
        String[] specials = {"#", "@", "%", "&", "(", ")", "[", "]"};

        String theTopic = (topic == null || topic.isBlank()) ? "everyday life" : topic.trim();
        String[] topicWords = theTopic.split("\\s+");

        List<String> words = new ArrayList<>(targetWords + 20);

        words.add(cap(theTopic));
        words.add("is");
        words.add("the");
        words.add("focus");
        words.add("of");
        words.add("this");
        words.add("typing");
        words.add("practice");
        addSentence(words, "This lesson explores " + theTopic + " with a focus on practical understanding and core terminology.");
        addSentence(words, "As you progress, note the definitions, historical context, and everyday relevance described here.");

        while (words.size() < targetWords) {
            String w;
            if (r.nextDouble() < 0.18 && topicWords.length > 0) {
                w = topicWords[r.nextInt(topicWords.length)];
            } else {
                int pick = r.nextInt(3);
                if (pick == 0) w = common[r.nextInt(common.length)];
                else if (pick == 1) w = verbs[r.nextInt(verbs.length)];
                else w = nouns[r.nextInt(nouns.length)];
            }

            if (includeUpper && r.nextDouble() < 0.12) {
                w = w.toUpperCase(Locale.ROOT);
            }
            if (includeNumbers && r.nextDouble() < 0.10) {
                w = w + " " + (10 + r.nextInt(89));
            }
            if (includeSpecial && r.nextDouble() < 0.06) {
                w = w + specials[r.nextInt(specials.length)];
            }
            words.add(w);
            if (includePunct && r.nextDouble() < 0.12) {
                words.add(punct[r.nextInt(punct.length)]);
            }
        }

        if (!words.get(words.size() - 1).matches("[.!?]")) {
            words.add(".");
        }

        StringBuilder sb = new StringBuilder();
        boolean afterPunct = false;
        for (String w : words) {
            if (w.matches("[,;.!?]")) {
                sb.append(w);
                afterPunct = true;
            } else {
                if (sb.length() == 0 || afterPunct) {
                    if (sb.length() > 0) sb.append(" ");
                    sb.append(cap(w));
                } else {
                    sb.append(" ").append(w);
                }
                afterPunct = false;
            }
        }
        String raw = sb.toString();
        return PassageConstraintEnforcer.enforce(
                raw, includeUpper, includeNumbers, includePunct, includeSpecial);
    }

    private static String cap(String s) {
        // Simple capitalisation helper so sentences look intentional.
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + (s.length() > 1 ? s.substring(1) : "");
    }

    private static void addSentence(List<String> words, String sentence) {
        // Seed the passage with a consistent intro paragraph before randomness takes over.
        if (sentence == null || sentence.isBlank()) return;
        for (String token : sentence.trim().split("\\s+")) {
            words.add(token);
        }
    }
}
