package itam.mx.packages.alpha_version_final;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class GameApplication extends Application {
    private int tamanio_tablero;
    public GameApplication(int tamanio_tablero){
        this.tamanio_tablero = tamanio_tablero;
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Canvas e ico
        FXMLLoader fxmlLoader = new FXMLLoader(GameApplication.class.getResource("game-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Whac-A-Mole");
        stage.getIcons().add(new Image("file:src/main/java/itam/mx/packages/alpha_version_final/Whac-A-Mole.png"));
        stage.setScene(scene);
        stage.show();


    }

}