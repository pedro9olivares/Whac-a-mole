package itam.mx.packages.alpha_version_final;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    public static Cliente cliente;
    @FXML
    private Label login_label;

    @FXML
    private TextField username_input;

    @FXML
    private Button play_button;

    @FXML
    private TextField lobby_server_ip_txt;

    @FXML
    private TextField lobby_server_port_txt;

    @FXML
    protected void onPlayButtonClick() throws IOException {
        String input = username_input.getText();
        String lobby_ip = lobby_server_ip_txt.getText();
        String lobby_port_s = lobby_server_port_txt.getText();
        int lobby_port;

        if(input.equals("") || input.contains(" ") || lobby_ip.equals("") || lobby_port_s.equals("")){
            login_label.setText("Please enter a valid username with no spaces:");
        } else {
            lobby_port = Integer.parseInt(lobby_port_s);
            LobbiesApplication lobbies = new LobbiesApplication();

            // Connect to login server
            cliente = new Cliente(input, lobby_port, lobby_ip);
            cliente.connectToLoginServer();

            try {
                lobbies.start(new Stage());
                Stage stage = (Stage) play_button.getScene().getWindow();
                stage.close();
            } catch (Exception e){
                System.out.println("Hubo un error: "+e);
            }
        }

    }
}
