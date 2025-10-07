package typingNinja.ai;

import java.util.Locale;
import java.util.Set;

/**
 * Normalises generated passages so that they strictly obey the modifier flags supplied by the user.
 * It removes any disallowed character types and ensures at least one example is present for each
 * modifier that is enabled.
 */
final class PassageConstraintEnforcer {
    private static final Set<Character> PUNCTUATION = Set.of(
            '.', ',', ';', ':', '!', '?', '\'', '"', '-'
    );
    private static final Set<Character> SPECIALS = Set.of(
            '#', '@', '$', '%', '^', '&', '*', '(', ')',
            '[', ']', '{', '}', '<', '>', '~', '`', '|',
            '\\', '/', '_', '+', '='
    );

    private PassageConstraintEnforcer() { }

    static String enforce(String text,
                          boolean includeUpper,
                          boolean includeNumbers,
                          boolean includePunct,
                          boolean includeSpecial) {
        if (text == null) text = "";

        StringBuilder cleaned = new StringBuilder(text.length());
        boolean lastWasWhitespace = true;

        boolean hasUpper = false;
        boolean hasNumber = false;
        boolean hasPunct = false;
        boolean hasSpecial = false;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            char out = ch;

            if (Character.isUpperCase(ch)) {
                if (includeUpper) {
                    out = ch;
                } else {
                    out = Character.toLowerCase(ch);
                }
            } else if (Character.isDigit(ch)) {
                if (!includeNumbers) {
                    out = ' ';
                }
            } else if (isPunctuation(ch)) {
                if (!includePunct) {
                    out = ' ';
                }
            } else if (isSpecial(ch)) {
                if (!includeSpecial) {
                    out = ' ';
                }
            }

            if (Character.isUpperCase(out)) hasUpper = true;
            if (Character.isDigit(out)) hasNumber = true;
            if (isPunctuation(out)) hasPunct = true;
            if (isSpecial(out)) hasSpecial = true;

            if (Character.isWhitespace(out)) {
                if (!lastWasWhitespace && cleaned.length() > 0) {
                    cleaned.append(' ');
                }
                lastWasWhitespace = true;
            } else {
                cleaned.append(out);
                lastWasWhitespace = false;
            }
        }

        String result = cleaned.toString().trim();
        if (result.isEmpty()) {
            result = "typing practice passage";
        }

        if (!includeUpper) {
            result = result.toLowerCase(Locale.ROOT);
            hasUpper = false;
        } else if (!hasUpper) {
            result = ensureUppercase(result);
            hasUpper = true;
        }

        if (!includeNumbers) {
            result = result.replaceAll("\\d+", " ").replaceAll("\\s+", " ").trim();
        } else if (!hasNumber) {
            result = result + " 123";
        }

        if (!includePunct) {
            result = stripCharacters(result, PUNCTUATION);
        } else if (!hasPunct) {
            result = appendWithSpace(result, ".");
        }

        if (!includeSpecial) {
            result = stripCharacters(result, SPECIALS);
        } else if (!hasSpecial) {
            result = appendWithSpace(result, "#");
        }

        return result.replaceAll("\\s+", " ").trim();
    }

    private static boolean isPunctuation(char ch) {
        return PUNCTUATION.contains(ch);
    }

    private static boolean isSpecial(char ch) {
        return SPECIALS.contains(ch);
    }

    private static String ensureUppercase(String text) {
        StringBuilder sb = new StringBuilder(text);
        for (int i = 0; i < sb.length(); i++) {
            char ch = sb.charAt(i);
            if (Character.isLetter(ch)) {
                sb.setCharAt(i, Character.toUpperCase(ch));
                return sb.toString();
            }
        }
        return text.toUpperCase(Locale.ROOT);
    }

    private static String stripCharacters(String input, Set<Character> disallowed) {
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (!disallowed.contains(ch)) {
                sb.append(ch);
            } else {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    private static String appendWithSpace(String base, String token) {
        if (base.isEmpty()) return token;
        if (token.length() == 1 && isPunctuation(token.charAt(0))) {
            return base + token;
        }
        if (Character.isWhitespace(base.charAt(base.length() - 1))) {
            return (base + token).trim();
        }
        return base + " " + token;
    }
}
