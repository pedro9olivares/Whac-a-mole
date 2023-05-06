package itam.mx.packages.alpha_version_final;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class LobbiesController {
    public static int tamanio_tablero;
    public static String current_lobby_name;
    public static String current_lobby_ip;
    public static String current_lobby_socket_port;
    public static String current_lobby_amq_port;

    private Cliente cliente;
    @FXML
    private Button lobby1_button;

    @FXML
    private Button lobby2_button;

    @FXML
    private Button lobby3_button;

    @FXML
    protected void initialize(){
        cliente = LoginController.cliente;
        System.out.println("\nBeggining lobbies");

        String[] info_lobby_1 = cliente.lobbys.get(2).split(",");
        String[] info_lobby_2 = cliente.lobbys.get(1).split(",");
        String[] info_lobby_3 = cliente.lobbys.get(0).split(",");

        lobby1_button.setText(info_lobby_1[0] + ": "+info_lobby_1[4]+"x"+info_lobby_1[5]);
        lobby2_button.setText(info_lobby_2[0] + ": "+info_lobby_2[4]+"x"+info_lobby_2[5]);
        lobby3_button.setText(info_lobby_3[0] + ": "+info_lobby_3[4]+"x"+info_lobby_3[5]);
    }
    @FXML
    protected void on_lobby_1_button_click() throws IOException {
        String[] info_lobby_1 = cliente.lobbys.get(2).split(",");

        tamanio_tablero = Integer.parseInt(info_lobby_1[4]);
        current_lobby_name = info_lobby_1[0];
        current_lobby_ip = info_lobby_1[1];
        current_lobby_amq_port = info_lobby_1[2];
        current_lobby_socket_port = info_lobby_1[3];


        GameApplication game = new GameApplication(tamanio_tablero);
        game.start(new Stage());

        Stage stage = (Stage) lobby1_button.getScene().getWindow();
        stage.close();

        System.out.println("\nSe inició un juego con tablero de tamaño: " + tamanio_tablero);
    }

    @FXML
    protected void on_lobby_2_button_click() throws IOException{
        String[] info_lobby_2 = cliente.lobbys.get(1).split(",");

        tamanio_tablero = Integer.parseInt(info_lobby_2[4]);
        current_lobby_name = info_lobby_2[0];
        current_lobby_ip = info_lobby_2[1];
        current_lobby_amq_port = info_lobby_2[2];
        current_lobby_socket_port = info_lobby_2[3];


        GameApplication game = new GameApplication(tamanio_tablero);
        game.start(new Stage());

        Stage stage = (Stage) lobby2_button.getScene().getWindow();
        stage.close();

        System.out.println("\nSe inició un juego con tablero de tamaño: " + tamanio_tablero);
    }

    @FXML
    protected void on_lobby_3_button_click() throws IOException{
        String[] info_lobby_3 = cliente.lobbys.get(0).split(",");

        tamanio_tablero = Integer.parseInt(info_lobby_3[4]);
        current_lobby_name = info_lobby_3[0];
        current_lobby_ip = info_lobby_3[1];
        current_lobby_amq_port = info_lobby_3[2];
        current_lobby_socket_port = info_lobby_3[3];


        GameApplication game = new GameApplication(tamanio_tablero);
        game.start(new Stage());

        Stage stage = (Stage) lobby3_button.getScene().getWindow();
        stage.close();

        System.out.println("\nSe inició un juego con tablero de tamaño: " + tamanio_tablero);
    }

}
