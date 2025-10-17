package typingNinja.model.ai;

public interface AITextService {
    // Contract for generating lesson text with togglable character constraints.
    String generatePassage(String topic, int targetWords,
                           boolean includeUpper, boolean includeNumbers,
                           boolean includePunct, boolean includeSpecial) throws Exception;
}
