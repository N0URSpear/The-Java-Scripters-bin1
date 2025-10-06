package com.example.addressbook.controllers;

import com.example.addressbook.MainMenu;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;


public class NinjaController {

    @FXML private ImageView logoImage;
    @FXML private StackPane backgroundPane;
    @FXML private Button loginButton, createAccountButton, helpButton;

    /**
     * Set up the initial screen.
     */
    @FXML
    public void initialize() {
        backgroundPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {

                DoubleBinding scaleFactor = Bindings.createDoubleBinding(
                        () -> Math.min(newScene.getWidth(), newScene.getHeight()),
                        newScene.widthProperty(),
                        newScene.heightProperty()
                );

                // Background image
                logoImage.fitWidthProperty().bind(newScene.widthProperty().multiply(0.9)); // Scaling control
                logoImage.fitHeightProperty().bind(newScene.heightProperty().multiply(0.9)); // Scaling control

                // Buttons
                for (Button btn : new Button[]{loginButton, createAccountButton, helpButton}) {
                    btn.styleProperty().bind(
                            Bindings.createStringBinding(() -> {
                                double fontSize = scaleFactor.get() * 0.03; // adjust font size
                                return String.format(
                                        "-fx-font-size: %.0fpx;",
                                        fontSize
                                );
                            }, newScene.widthProperty(), newScene.heightProperty())
                    );
                }

                loginButton.layoutXProperty().bind(
                        Bindings.createDoubleBinding(
                                () -> newScene.getWidth() * 0.1 - loginButton.getWidth() / 2,
                                newScene.widthProperty(),
                                loginButton.widthProperty()
                        )
                ); // 10% from left
                loginButton.layoutYProperty().bind(newScene.heightProperty().multiply(0.92)); // 92% from top

                createAccountButton.layoutXProperty().bind(
                        Bindings.createDoubleBinding(
                                () -> newScene.getWidth() * 0.5 - createAccountButton.getWidth() / 2,
                                newScene.widthProperty(),
                                loginButton.widthProperty()
                        )
                ); // 50% from left
                createAccountButton.layoutYProperty().bind(newScene.heightProperty().multiply(0.92)); // 92% from top

                helpButton.layoutXProperty().bind(
                        Bindings.createDoubleBinding(
                                () -> newScene.getWidth() * 0.9 - helpButton.getWidth() / 2,
                                newScene.widthProperty(),
                                loginButton.widthProperty()
                        )
                ); // 90% from left
                helpButton.layoutYProperty().bind(newScene.heightProperty().multiply(0.92)); // 92% from top
            }
        });
    }

    /**
     * Opens a login popup when the login button is clicked. transitions to the main meno, or to the forgot password popup depending on user inputs.
     *
     * @throws IOException if inputs or outputs are invalid
     */
    public void onloginClicked() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/addressbook/Login-view.fxml"));
        Stage loginPopup = new Stage();
        Parent root = fxmlLoader.load();
        LoginController controller = fxmlLoader.getController();
        loginPopup.setTitle("Login");
        loginPopup.initModality(Modality.APPLICATION_MODAL);
        loginPopup.setScene(new Scene(root));
        loginPopup.setResizable(false);
        loginPopup.initStyle(StageStyle.UNDECORATED);
        loginPopup.showAndWait();

        if (controller.isLoginSuccessful()) {
            Stage stage = (Stage) logoImage.getScene().getWindow();
            MainMenu menu = new MainMenu();
            Scene mainMenuScene = menu.buildScene(stage);
            stage.setScene(mainMenuScene);
            stage.setTitle("Main Menu - Typing Ninja");
        }

        if (controller.isForgotPassword()) {
            FXMLLoader fxmlLoader1 = new FXMLLoader(getClass().getResource("/com/example/addressbook/ForgorPassword-view.fxml"));
            Stage forgotPasswordPopup = new Stage();
            Parent root1 = fxmlLoader1.load();
            forgotPasswordPopup.setTitle("Forgot Password");
            forgotPasswordPopup.initModality(Modality.APPLICATION_MODAL);
            forgotPasswordPopup.setScene(new Scene(root1));
            forgotPasswordPopup.setResizable(false);
            forgotPasswordPopup.initStyle(StageStyle.UNDECORATED);
            forgotPasswordPopup.showAndWait();
        }
    }

    /**
     * Opens the account creation popup. If successful, transitions to the main menu.
     *
     * @throws IOException if inputs or outputs are invalid
     */
    public void onCreateAccountClicked() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/addressbook/AccountCreation-view.fxml"));
        Stage AccountCreation = new Stage();
        Parent root = fxmlLoader.load();
        CreateAccountController controller = fxmlLoader.getController();
        AccountCreation.setTitle("Login");
        AccountCreation.initModality(Modality.APPLICATION_MODAL);
        AccountCreation.setScene(new Scene(root));
        AccountCreation.setResizable(false);
        AccountCreation.initStyle(StageStyle.UNDECORATED);
        AccountCreation.showAndWait();

        if (controller.isCreateAccountSuccessful()) {
            Stage stage = (Stage) logoImage.getScene().getWindow();
            MainMenu menu = new MainMenu();
            Scene mainMenuScene = menu.buildScene(stage);
            stage.setScene(mainMenuScene);
            stage.setTitle("Main Menu - Typing Ninja");
        }
    }

    /**
     * Loads the help popup when clicked.
     *
     * @throws IOException if input or output is invalid
     */
    public void onHelpClicked() throws IOException{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/addressbook/Help-view.fxml"));
        Stage HelpPopup = new Stage();
        HelpPopup.setTitle("Help");
        HelpPopup.initModality(Modality.APPLICATION_MODAL);
        HelpPopup.setScene(new Scene(fxmlLoader.load()));
        HelpPopup.setResizable(false);
        HelpPopup.showAndWait();
    }
}
