module com.dreamstop {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.dreamstop.common;
    requires com.google.gson;

    opens com.dreamstop to javafx.fxml;
    exports com.dreamstop;
}
