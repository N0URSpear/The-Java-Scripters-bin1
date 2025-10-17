package typingNinja.model.ai;

/**
 * Strategy interface for generating lesson passages with optional character constraints.
 */
public interface AITextService {
    /**
     * Generates a passage that fits the requested toggles.
     *
     * @param topic textual topic or hint for the provider
     * @param targetWords rough word budget to aim for
     * @param includeUpper whether uppercase letters should feature
     * @param includeNumbers whether digits should feature
     * @param includePunct whether punctuation should feature
     * @param includeSpecial whether special characters should feature
     * @return generated passage text
     * @throws Exception when the provider cannot supply text
     */
    String generatePassage(String topic, int targetWords,
                           boolean includeUpper, boolean includeNumbers,
                           boolean includePunct, boolean includeSpecial) throws Exception;
}
