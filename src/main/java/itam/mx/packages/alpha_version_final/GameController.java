package itam.mx.packages.alpha_version_final;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;

public class GameController {

    @FXML
    private AnchorPane progress_pane;
    @FXML
    public static CheckBox[][] moles;

    @FXML
    private GridPane tablero;

    @FXML
    private GridPane canvas;
    @FXML
    private Label score;

    @FXML
    private Button back_to_lobbies_button;

    @FXML
    private Button start_button;

    @FXML
    public static ProgressBar progress_bar;

    @FXML
    protected void initialize() {
        // Genera el tablero de tamaño adecuado
        int tam_tablero = LobbiesController.tamanio_tablero;
        tablero = new GridPane();
        tablero.setAlignment(Pos.CENTER);
        moles = new CheckBox[tam_tablero][tam_tablero];
        progress_bar = new ProgressBar(0);
        progress_bar.setPadding(new Insets(3,0,0,0));
        progress_pane.getChildren().add(progress_bar);


        canvas.add(tablero, 0, 2);
        for (int i = 0; i < tam_tablero; i++) {
            tablero.addColumn(i);
            tablero.addRow(i);
        }

        tablero.getColumnConstraints().addAll(new ColumnConstraints(100));
        tablero.getRowConstraints().addAll(new RowConstraints(30));

        for (int i=0; i<tam_tablero; i++){
            for(int j=0; j<tam_tablero; j++){
                //System.out.println(i);
                //System.out.println(j);
                moles[i][j] = new CheckBox();
                //System.out.println("Trying to set id:"+i+","+j);
                String coords = String.valueOf(i)+String.valueOf(j);
                //System.out.println("Setted: "+coords);
                moles[i][j].setId(coords);
                moles[i][j].getStylesheets().add("file:src/main/resources/itam/mx/packages/alpha_version_final/moles.css");
                tablero.add(moles[i][j],i,j);
                moles[i][j].setOnAction(eh);
            }
        }
    }

    EventHandler eh = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            CheckBox chk = (CheckBox) event.getSource();
            if (event.getSource() instanceof CheckBox) {
                String coords = chk.getId();
                // Si el checkbox estaba vacío, revertir
                if(chk.isSelected()){
                    chk.setSelected(false);
                }else{
                    int i = Character.getNumericValue(coords.charAt(0));
                    int j = Character.getNumericValue(coords.charAt(1));
                    smash(j, i); // No sé por qué se me volteó?
                }
            }
        }
    };

    private void smash(int i, int j){
        Cliente cliente = LoginController.cliente;
        cliente.smash(cliente.username, i, j, cliente.randN, Integer.parseInt(LobbiesController.current_lobby_socket_port));
    }

    @FXML
    protected void on_back_to_lobbies_clicked() throws IOException {
        LobbiesApplication lobbies = new LobbiesApplication();
        lobbies.start(new Stage());

        Stage stage = (Stage) back_to_lobbies_button.getScene().getWindow();
        stage.close();
    }
    @FXML
    protected void start_thread(){
        /* El juego */
        LoginController.cliente.start(); // TODO: cerrar hilo onClose
        start_button.setDisable(true);
        back_to_lobbies_button.setDisable(true);

    }

    @FXML
    public void exitApplication(ActionEvent event) {
        Platform.exit();
        LoginController.cliente.interrupt();
    }



}