module com.dreamstop {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.logging;
    requires com.dreamstop.common;
    requires com.google.gson;

    opens com.dreamstop to javafx.fxml;
    opens com.dreamstop.controller to javafx.fxml;
    opens com.dreamstop.model to javafx.base;

    exports com.dreamstop;
    exports com.dreamstop.controller;
    exports com.dreamstop.model;
    exports com.dreamstop.service;
    exports com.dreamstop.network;
}