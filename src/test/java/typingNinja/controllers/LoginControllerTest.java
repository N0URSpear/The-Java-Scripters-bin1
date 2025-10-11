package typingNinja.controllers;

import typingNinja.INinjaContactDAO;
import typingNinja.MockNinjaDAO;
import typingNinja.NinjaUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginControllerTest {

    private LoginController controller;
    private INinjaContactDAO mockDao;

    @BeforeEach
    void setUp() {

        // reset the in-memory DAO between tests
        MockNinjaDAO.clearAll();
        mockDao = new MockNinjaDAO();
        controller = new LoginController(mockDao);
        controller.setTestMode(true);
    }

    @Test
    void loginFailsWhenUserNotFound() {
        controller.doLogin("notaUser","Password");
        assertFalse(controller.isLoginSuccessful());
    }

    @Test
    void loginFailsWithWrongPassword() {
        NinjaUser user = new NinjaUser("testUser", BCrypt.hashpw("correctPass", BCrypt.gensalt()),"Q1","Q2","Q1","Q2");
        mockDao.addNinjaUser(user);

        controller.doLogin("testUser", "wrongPass");

        assertFalse(controller.isLoginSuccessful());
    }

    @Test
    void loginSucceedsWithCorrectPassword() {
        NinjaUser user = new NinjaUser("testUser", BCrypt.hashpw("correctPass", BCrypt.gensalt()),"Q1","Q2","Q1","Q2");
        mockDao.addNinjaUser(user);

        controller.doLogin("testUser", "correctPass");

        assertTrue(controller.isLoginSuccessful());
    }

    @Test
    void forgotPasswordSetsFlag() {
        controller.ForgotPassword();
        assertTrue(controller.isForgotPassword());
    }
}
