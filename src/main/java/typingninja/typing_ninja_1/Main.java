package typingninja.typing_ninja_1;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        Scene first = CongratulationsScene.createScene(stage); // 初始场景：Congratulations
        stage.setTitle("Typing Ninja");
        stage.setScene(first);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
