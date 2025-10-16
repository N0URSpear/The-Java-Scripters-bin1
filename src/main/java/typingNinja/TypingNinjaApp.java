package typingNinja;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.io.IOException;
import typingNinja.util.SceneNavigator;

public class TypingNinjaApp extends Application {

    public static final String TITLE = "TYPING NINJA";

    @Override
    public void start(Stage stage) throws IOException {

        // Load Jaro font for stylesheets and FXML usag
        Font.loadFont(getClass().getResourceAsStream("/typingNinja/Fonts/Jaro-Regular-VariableFont_opsz.ttf"), 14);

        // Load FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/typingNinja/Ninja-view.fxml"));
        Parent root = fxmlLoader.load();

        SceneNavigator.show(stage, root, TITLE);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
