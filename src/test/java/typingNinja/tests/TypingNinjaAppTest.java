package typingNinja.tests;

import org.junit.jupiter.api.Test;
import typingNinja.TypingNinjaApp;

import static org.junit.jupiter.api.Assertions.*;

class TypingNinjaAppTest {

    @Test
    void classShouldBeInstantiable() {
        TypingNinjaApp app = new TypingNinjaApp();
        assertNotNull(app);
    }

    @Test
    void titleConstantShouldMatchSpec() {
        assertEquals("TYPING NINJA", TypingNinjaApp.TITLE);
    }
}
