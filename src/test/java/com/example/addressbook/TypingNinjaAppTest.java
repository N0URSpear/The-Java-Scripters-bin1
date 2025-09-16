package com.example.addressbook;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TypingNinjaAppTest {

    @Test
    void classShouldBeInstantiable() {
        // 不调用 Application.launch，只验证能 new 出来（构造器不抛异常）
        TypingNinjaApp app = new TypingNinjaApp();
        assertNotNull(app);
    }

    @Test
    void titleConstantShouldMatchSpec() {
        assertEquals("TYPING NINJA", TypingNinjaApp.TITLE);
    }


}
