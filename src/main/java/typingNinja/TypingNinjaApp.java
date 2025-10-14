package typingNinja;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.io.IOException;

public class TypingNinjaApp extends Application {

    public static final String TITLE = "TYPING NINJA";

    @Override
    public void start(Stage stage) throws IOException {

        // Load Jaro font for stylesheets and FXML usag
        Font.loadFont(getClass().getResourceAsStream("/typingNinja/Fonts/Jaro-Regular-VariableFont_opsz.ttf"), 14);

        // Load FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/typingNinja/Ninja-view.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);
        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
