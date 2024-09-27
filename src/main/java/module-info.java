module org.example.paintfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires java.desktop;
    requires jdk.httpserver;

    opens org.example.paintfx to javafx.fxml;
    exports org.example.paintfx;
}