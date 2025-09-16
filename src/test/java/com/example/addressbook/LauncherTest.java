package com.example.addressbook;

import org.junit.jupiter.api.Test;

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

    // 如需真正调用（会启动 UI），请在手测/集成阶段做，单元测试保持快速与无 UI。
    // @Test
    // void mainShouldNotThrow_whenInvokedWithNoArgs() {
    //     assertDoesNotThrow(() -> Launcher.main(new String[]{}));
    // }
}
