package typingNinja.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import typingNinja.view.MainMenu;

/**
 * Controller for the legacy typing scene that offers a quick return to the menu.
 */
public class TypingController {

    @FXML
    /**
     * Navigates back to the main menu when the back button is pressed.
     */
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
