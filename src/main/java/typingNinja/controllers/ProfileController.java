package typingNinja.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;
import typingNinja.view.MainMenu;

public class ProfileController {

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            // Get the current stage (the Profile window)
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            // Rebuild the Main Menu scene
            MainMenu mainMenu = new MainMenu();
            Scene scene = mainMenu.buildScene(stage);

            // Show Main Menu
            stage.setScene(scene);
            stage.setTitle("Main Menu - Typing Ninja");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
