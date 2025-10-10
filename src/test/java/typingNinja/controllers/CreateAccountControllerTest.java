package com.example.addressbook.controllers;

import com.example.addressbook.INinjaContactDAO;
import com.example.addressbook.MockNinjaDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CreateAccountControllerTest {

    private CreateAccountController controller;
    private INinjaContactDAO mockDao;

    @BeforeEach
    void setUp() {

        // reset the in-memory DAO between tests
        MockNinjaDAO.clearAll();
        mockDao = new MockNinjaDAO();
        controller = new CreateAccountController(mockDao);
        controller.setTestMode(true);
    }

    @Test
    void emptyUsernameFails() {
        controller.doCreateAccount("", "pass", "pass", "Q1", "Q2", "a1", "a2");
        assertFalse(controller.isCreateAccountSuccessful());
        assertEquals(0, mockDao.getAllNinjas().size());
    }

    @Test
    void emptyPasswordFails() {
        controller.doCreateAccount("user", "", "", "Q1", "Q2", "a1", "a2");
        assertFalse(controller.isCreateAccountSuccessful());
        assertEquals(0, mockDao.getAllNinjas().size());
    }

    @Test
    void passwordsDontMatchFails() {
        controller.doCreateAccount("user", "pass", "wrong", "Q1", "Q2", "a1", "a2");
        assertFalse(controller.isCreateAccountSuccessful());
        assertEquals(0, mockDao.getAllNinjas().size());
    }

    @Test
    void sameSecretQuestionsFails() {
        controller.doCreateAccount("user", "pass", "pass", "Q1", "Q1", "a1", "a2");
        assertFalse(controller.isCreateAccountSuccessful());
        assertEquals(0, mockDao.getAllNinjas().size());
    }

    @Test
    void missingAnswersFails() {
        controller.doCreateAccount("user", "pass", "pass", "Q1", "Q2", "", "a2");
        assertFalse(controller.isCreateAccountSuccessful());
        assertEquals(0, mockDao.getAllNinjas().size());
    }

    @Test
    void duplicateUsernameFails() {
        // Add first user
        controller.doCreateAccount("user", "pass", "pass", "Q1", "Q2", "a1", "a2");
        assertTrue(controller.isCreateAccountSuccessful());
        assertEquals(1, mockDao.getAllNinjas().size());

        // Try to add second user with same username
        controller.doCreateAccount("user", "pass2", "pass2", "Q1", "Q2", "a1", "a2");
        assertFalse(controller.isCreateAccountSuccessful()); // stays false after second attempt
        assertEquals(1, mockDao.getAllNinjas().size());
    }

    @Test
    void validAccountCreationSucceeds() {
        controller.doCreateAccount("newUser", "pass", "pass", "Q1", "Q2", "a1", "a2");
        assertTrue(controller.isCreateAccountSuccessful());
        assertEquals(1, mockDao.getAllNinjas().size());
        assertEquals("newUser", mockDao.getAllNinjas().get(0).getUserName());
    }
}
