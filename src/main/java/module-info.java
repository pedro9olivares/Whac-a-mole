module itam.mx.packages.alpha_version_final {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires activemq.client;
    requires javax.jms;
    requires java.naming;

    opens itam.mx.packages.alpha_version_final to javafx.fxml;
    exports itam.mx.packages.alpha_version_final;
}