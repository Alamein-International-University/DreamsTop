module com.dreamstop.common {
    requires transitive com.google.gson;

    opens com.dreamstop.common.model to com.google.gson;
    opens com.dreamstop.common.dto to com.google.gson;
    opens com.dreamstop.common.protocol to com.google.gson;

    exports com.dreamstop.common.model;
    exports com.dreamstop.common.dto;
    exports com.dreamstop.common.protocol;
}
