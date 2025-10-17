package typingNinja.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import typingNinja.view.MainMenu;

public class TypingController {

    @FXML
    private void handleBack(ActionEvent event) {
        // Return to the main menu view when the user exits the typing scene.
        try {
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            MainMenu mainMenu = new MainMenu();
            mainMenu.show(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
