package com.example.addressbook;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;


public class NinjaController {

    @FXML private ImageView logoImage;
    @FXML private StackPane backgroundPane;
    @FXML private Button loginButton, createAccountButton, helpButton;

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
                                double fontSize = scaleFactor.get() * 0.03; // adjust proportionally
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

    public void onloginClicked(ActionEvent actionEvent) {

    }

    public void onCreateAccountClicked(ActionEvent actionEvent) {
    }

    public void onHelpClicked(ActionEvent actionEvent) {
    }
}
