package typingNinja.tests;

import org.junit.jupiter.api.Test;
import typingNinja.model.NinjaUser;

import static org.junit.jupiter.api.Assertions.*;

class NinjaUserTest {

    @Test
    void constructorAndGettersShouldMatch() {
        NinjaUser u = new NinjaUser("alice", "hash",
                "Q1", "Q2", "A1", "A2");

        assertEquals("alice", u.getUserName());
        assertEquals("hash", u.getPasswordHash());
        assertEquals("Q1", u.getSecretQuestion1());
        assertEquals("Q2", u.getSecretQuestion2());
        assertEquals("A1", u.getSecretQuestion1Answer());
        assertEquals("A2", u.getSecretQuestion2Answer());
    }

    @Test
    void settersShouldChangeMutableFields() {
        NinjaUser u = new NinjaUser("alice", "hash",
                "Q1", "Q2", "A1", "A2");

        u.setPasswordHash("newHash");
        assertEquals("newHash", u.getPasswordHash());
    }

    @Test
    void toStringShouldNotBeNull() {
        NinjaUser u = new NinjaUser("alice", "hash",
                "Q1", "Q2", "A1", "A2");
        assertNotNull(u.toString());
    }
}
