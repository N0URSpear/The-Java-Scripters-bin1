package typingNinja.tests.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import typingNinja.controllers.CreateAccountController;
import typingNinja.model.INinjaContactDAO;
import typingNinja.model.MockNinjaDAO;

import static org.junit.jupiter.api.Assertions.*;

public class CreateAccountControllerTest {

    private CreateAccountController controller;
    private INinjaContactDAO mockDao;

    @BeforeEach
    void setUp() {
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
        controller.doCreateAccount("user", "pass", "pass", "Q1", "Q2", "a1", "a2");
        assertTrue(controller.isCreateAccountSuccessful());
        assertEquals(1, mockDao.getAllNinjas().size());

        controller.doCreateAccount("user", "pass2", "pass2", "Q1", "Q2", "a1", "a2");
        assertFalse(controller.isCreateAccountSuccessful());
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
