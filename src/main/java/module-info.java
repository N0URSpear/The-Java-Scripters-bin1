open module typingNinja {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires jbcrypt;
    requires java.sql;
    requires javafx.graphics;
    requires javafx.media;
    requires itextpdf;
    requires javafx.swing;

    requires com.google.gson;
    requires java.net.http;
    requires org.apache.pdfbox;

    exports typingNinja;
    exports typingNinja.controllers;
}
