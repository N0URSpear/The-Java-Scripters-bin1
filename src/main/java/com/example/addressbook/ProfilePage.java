package com.example.addressbook;

import com.example.addressbook.controllers.ProfilePageController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class ProfilePage {

    private final Parent root;
    private final ProfilePageController controller;

    public ProfilePage(int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/addressbook/ProfilePage.fxml"));
            this.root = loader.load();
            this.controller = loader.getController();
            this.controller.setUserId(userId);  // 把 userId 传给控制器
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public ProfilePageController getController() {
        return controller;
    }


}
