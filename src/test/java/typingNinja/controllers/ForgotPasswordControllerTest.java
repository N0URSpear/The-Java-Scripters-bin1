package com.example.addressbook.controllers;

import com.example.addressbook.INinjaContactDAO;
import com.example.addressbook.MockNinjaDAO;
import com.example.addressbook.NinjaUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.addressbook.controllers.ForgotPasswordController.ForgotPasswordResult;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

public class ForgotPasswordControllerTest {

    private ForgotPasswordController controller;
    private INinjaContactDAO mockDao;

    @BeforeEach
    void setUp() {

        // reset the in-memory DAO between tests
        MockNinjaDAO.clearAll();
        mockDao = new MockNinjaDAO();
        controller = new ForgotPasswordController(mockDao);
        NinjaUser testNinja = new NinjaUser("testUser", BCrypt.hashpw("testPassword", BCrypt.gensalt()), "testQ1", "testQ2", BCrypt.hashpw("testQ1Answer", BCrypt.gensalt()), BCrypt.hashpw("testQ2Answer", BCrypt.gensalt()));
        mockDao.addNinjaUser(testNinja);
        controller.setTestMode(true);
    }

    // Stage 1
    @Test
    void testStage1_emptyUsername() {
        assertEquals(ForgotPasswordResult.EMPTY_USERNAME, controller.validateStage1(""));
    }

    @Test
    void testStage1_userNotFound() {
        assertEquals(ForgotPasswordResult.USER_NOT_FOUND, controller.validateStage1("notUser"));
    }

    @Test
    void testStage1_success() {
        assertEquals(ForgotPasswordResult.SUCCESS, controller.validateStage1("testUser"));
    }

    // Stage 2
    @Test
    void stage2_blankAnswers() {
        controller.validateStage1("testUser"); // load ninja
        assertEquals(ForgotPasswordResult.EMPTY_SECRET_ANSWERS,
                controller.validateStage2("", ""));
    }

    @Test
    void stage2_wrongAnswers() {
        controller.validateStage1("testUser");
        assertEquals(ForgotPasswordResult.WRONG_ANSWERS,
                controller.validateStage2("wrong1", "wrong2"));
    }

    @Test
    void stage2_success() {
        controller.validateStage1("testUser");
        assertEquals(ForgotPasswordResult.SUCCESS,
                controller.validateStage2("testQ1Answer", "testQ2Answer"));
    }

    // Stage 3
    @Test
    void stage3_emptyPassword() {
        controller.validateStage1("testUser");
        assertEquals(ForgotPasswordResult.EMPTY_PASSWORD,
                controller.validateStage3("", "something"));
    }

    @Test
    void stage3_emptyConfirmation() {
        controller.validateStage1("testUser");
        assertEquals(ForgotPasswordResult.EMPTY_CONFIRMATION,
                controller.validateStage3("newPassword", ""));
    }

    @Test
    void stage3_passwordMismatch() {
        controller.validateStage1("testUser");
        assertEquals(ForgotPasswordResult.PASSWORDS_MISMATCH,
                controller.validateStage3("newPassword", "differentPassword"));
    }

    @Test
    void stage3_success_passwordUpdated() {
        controller.validateStage1("testUser");
        ForgotPasswordResult result = controller.validateStage3("newPassword", "newPassword");

        assertEquals(ForgotPasswordResult.SUCCESS, result);

        // fetch updated user from DAO
        NinjaUser updated = mockDao.getNinjaUser("testUser");
        assertNotNull(updated);
        assertNotEquals("testPassword", updated.getPasswordHash());
    }


}
