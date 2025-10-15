module com.example.addressbook {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires jbcrypt;
    requires java.sql;
    requires itextpdf;
    requires javafx.swing;


    opens com.example.addressbook to javafx.fxml;
    exports com.example.addressbook;
    exports com.example.addressbook.controllers;
    opens com.example.addressbook.controllers to javafx.fxml;
}