package typingNinja;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NinjaUserTest {

    @Test
    void constructorAndGettersShouldMatch() {
        NinjaUser u = new NinjaUser("alice", "hash",
                "Q1", "Q2", "A1", "A2");

        // id 通常由 DAO 赋值，这里只检验其余字段
        assertEquals("alice", u.getUserName());
        assertEquals("hash",  u.getPasswordHash());
        assertEquals("Q1",    u.getSecretQuestion1());
        assertEquals("Q2",    u.getSecretQuestion2());
        assertEquals("A1",    u.getSecretQuestion1Answer());
        assertEquals("A2",    u.getSecretQuestion2Answer());
    }

    @Test
    void settersShouldChangeMutableFields() {
        NinjaUser u = new NinjaUser("alice", "hash",
                "Q1", "Q2", "A1", "A2");

        u.setPasswordHash("newHash");
        assertEquals("newHash", u.getPasswordHash());

        // 如果类允许改名/答案等，就继续断言；若没有 setter 请删除这些断言
        // u.setUserName("alice2");
        // assertEquals("alice2", u.getUserName());
    }

    @Test
    void toStringShouldNotBeNull() {
        NinjaUser u = new NinjaUser("alice", "hash",
                "Q1", "Q2", "A1", "A2");
        assertNotNull(u.toString());
    }
}
