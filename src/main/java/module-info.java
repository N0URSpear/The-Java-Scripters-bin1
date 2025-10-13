module typingNinja {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires jbcrypt;
    requires java.sql;
    requires javafx.graphics;
    requires javafx.media;

    requires com.google.gson;
    requires java.net.http;
    requires org.apache.pdfbox;

    opens typingNinja to javafx.fxml;
    exports typingNinja;
    exports typingNinja.controllers;
    opens typingNinja.controllers to javafx.fxml;
}
