module com.murzik.keyboardrace {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.murzik.keyboardrace to javafx.fxml;
    exports com.murzik.keyboardrace;
}