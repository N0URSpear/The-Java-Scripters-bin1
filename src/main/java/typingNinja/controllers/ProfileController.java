package typingNinja.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
            mainMenu.show(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
