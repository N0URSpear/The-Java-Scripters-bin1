package com.example.addressbook.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class LocalSimpleTextService implements AITextService {

    @Override
    public String generatePassage(String topic, int targetWords,
                                  boolean includeUpper, boolean includeNumbers,
                                  boolean includePunct, boolean includeSpecial) {

        // seed by topic so “cheese” is repeatable
        long seed = (topic == null ? 0 : topic.toLowerCase(Locale.ROOT).hashCode());
        Random r = new Random(seed);

        if (targetWords < 60) targetWords = 60;
        if (targetWords > 400) targetWords = 400; // keep it sensible

        String[] common = ("the a an this that these those every some many few simple little great " +
                "clear quick slow bright soft careful steady common daily modern classic").split(" ");
        String[] verbs = ("is are seems feels makes shows brings gives builds improves explains helps " +
                "supports creates discovers explores compares measures connects").split(" ");
        String[] nouns = ("idea method habit exercise topic lesson passage keyboard finger hand screen " +
                "student practice result time number letter symbol example story step line").split(" ");
        String[] punct = {".", ".", ".", ".", ",", ";", ".", "!", ".", "."}; // mostly periods
        String[] specials = {"#", "@", "%", "&", "(", ")", "[", "]"};

        String theTopic = (topic == null || topic.isBlank()) ? "everyday life" : topic.trim();
        // a small set of topic words to mix in
        String[] topicWords = theTopic.split("\\s+");

        List<String> words = new ArrayList<>(targetWords + 20);

        // first sentence mentions the topic explicitly
        words.add(cap(theTopic));
        words.add("is");
        words.add("the");
        words.add("focus");
        words.add("of");
        words.add("this");
        words.add("typing");
        words.add("practice");

        // build the remaining words
        while (words.size() < targetWords) {
            // choose between a topic word or a generic word
            String w;
            if (r.nextDouble() < 0.18 && topicWords.length > 0) {
                w = topicWords[r.nextInt(topicWords.length)];
            } else {
                int pick = r.nextInt(3);
                if (pick == 0) w = common[r.nextInt(common.length)];
                else if (pick == 1) w = verbs[r.nextInt(verbs.length)];
                else w = nouns[r.nextInt(nouns.length)];
            }

            // style modifiers
            if (includeUpper && r.nextDouble() < 0.12) {
                w = w.toUpperCase(Locale.ROOT);
            }
            if (includeNumbers && r.nextDouble() < 0.10) {
                w = w + " " + (10 + r.nextInt(89)); // add a natural small number
            }
            if (includeSpecial && r.nextDouble() < 0.06) {
                w = w + specials[r.nextInt(specials.length)];
            }

            words.add(w);

            // occasionally end a sentence
            if (includePunct && r.nextDouble() < 0.12) {
                words.add(punct[r.nextInt(punct.length)]);
            }
        }

        // ensure we end with a period
        if (!words.get(words.size() - 1).matches("[.!?]")) {
            words.add(".");
        }

        // stitch into a passage with basic spacing
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
        return sb.toString();
    }

    private static String cap(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + (s.length() > 1 ? s.substring(1) : "");
    }
}
