module com.dreamstop {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.dreamstop to javafx.fxml;
    exports com.dreamstop;
}
