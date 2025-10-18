package typingNinja.tests;

import org.junit.jupiter.api.Test;
import typingNinja.Launcher;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class LauncherTest {

    @Test
    void mainMethodShouldExistAndBePublicStaticVoid() throws NoSuchMethodException {
        Method m = Launcher.class.getDeclaredMethod("main", String[].class);
        assertTrue(Modifier.isPublic(m.getModifiers()), "main must be public");
        assertTrue(Modifier.isStatic(m.getModifiers()), "main must be static");
        assertEquals(void.class, m.getReturnType(), "main must return void");
    }
}
