package com.example.addressbook.controllers;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ProfilePageControllerFXMLTest {

    private FXMLLoader loader;
    private ProfilePageController controller;

    @BeforeAll
    static void initFX() throws Exception {
        // 启动 JavaFX Toolkit，仅需一次
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        latch.await(3, TimeUnit.SECONDS);
    }

    @BeforeEach
    void loadFXML() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                loader = new FXMLLoader(getClass().getResource("/com/example/addressbook/ProfilePage.fxml"));
                Parent root = loader.load();
                controller = loader.getController();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testControllerInjected() {
        assertNotNull(controller, "Controller should be loaded from FXML");
    }

    @Test
    void testFXMLFieldsInjected() {
        Label usernameLabel = (Label) getPrivateField(controller, "usernameLabel");
        Label hoursGoalLabel = (Label) getPrivateField(controller, "hoursGoalLabel");
        Label beltLabel = (Label) getPrivateField(controller, "beltLabel");
        Label starsValueLabel = (Label) getPrivateField(controller, "starsValueLabel");

        assertAll("FXML injection check",
                () -> assertNotNull(usernameLabel, "usernameLabel should be injected"),
                () -> assertNotNull(hoursGoalLabel, "hoursGoalLabel should be injected"),
                () -> assertNotNull(beltLabel, "beltLabel should be injected"),
                () -> assertNotNull(starsValueLabel, "starsValueLabel should be injected")
        );
    }

    @Test
    void testInitializeCalled() {
        boolean initialized = (boolean) getPrivateField(controller, "initialized");
        assertTrue(initialized, "Controller.initialize() should have been called automatically");
    }

    /**
     * ✅ 通用反射工具方法：安全地读取 private 字段
     */
    private Object getPrivateField(Object obj, String fieldName) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException("Cannot access field: " + fieldName, e);
        }
    }
}
