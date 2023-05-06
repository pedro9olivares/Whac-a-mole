package itam.mx.packages.alpha_version_final;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Canvas e ico
        FXMLLoader fxmlLoader = new FXMLLoader(LoginApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 400);
        stage.setTitle("Whac-A-Mole");
        stage.getIcons().add(new Image("file:src/main/java/itam/mx/packages/alpha_front/Whac-A-Mole.png"));
        stage.setScene(scene);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
