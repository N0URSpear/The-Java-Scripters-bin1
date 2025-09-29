module typingninja.typing_ninja_1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires org.apache.pdfbox;


    opens typingninja.typing_ninja_1 to javafx.fxml;
    exports typingninja.typing_ninja_1;
}