package itam.mx.packages.alpha_version_final;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Topic;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import java.net.*;
import java.io.*;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class Cliente extends Thread{
    String username;
    int puertoLogin;
    String ipLogin;
    ArrayList<String> lobbys = null;
    static int randN;

    public Cliente(String username, int puertoLogin, String ipLogin){
        this.username = username;
        this.puertoLogin = puertoLogin;
        this.ipLogin = ipLogin;

        System.out.println("Se creó un usuario: "+this.username);
    }

    @Override
    public void run(){
        //Ingresar al login
        //connectToLoginServer();

        // Ingresar a un lobby y jugar
        /*
        String[] infoDeLobby = lobbys.get(2).split(",");
        String lobby_name = infoDeLobby[0];
        String lobby_ip = infoDeLobby[1];
        String lobby_amq_port = infoDeLobby[2];
        String lobby_socket_port = infoDeLobby[3];
        int rows = Integer.parseInt(infoDeLobby[4]);
        int cols = Integer.parseInt(infoDeLobby[5]);
        */


        //String url = lobby_ip+":8161";
        String url = ActiveMQConnection.DEFAULT_BROKER_URL;
        //String url = "http://127.0.0.1:8161/";
        try{
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
            Connection connection = connectionFactory.createConnection();
            //connection.start();

            System.out.println("Started connection at URL: " + url);

            MessageConsumer messageConsumer;
            TextMessage textMessage;
            boolean endGameSession = false;

            Session session = connection.createSession(false /*Transacter*/, Session.AUTO_ACKNOWLEDGE);

            Topic topic = session.createTopic(LobbiesController.current_lobby_name); // El topic es el nombre del lobby
            messageConsumer = session.createConsumer(topic);
            connection.start();

            System.out.println("Consuming topic: "+LobbiesController.current_lobby_name);

            while(!endGameSession){
                // Juega
                textMessage = (TextMessage) messageConsumer.receive();
                System.out.println("Received");
                String recieved = textMessage.getText();
                System.out.println(recieved);

                //Splittea los datos
                String[] splitted_string = recieved.split(",");
                String randN = "-1";
                if(splitted_string.length == 4){
                    String ronda = splitted_string[0];
                    randN = splitted_string[1];
                    this.randN = Integer.parseInt(randN);
                    int r = Integer.parseInt(splitted_string[2]);
                    int c = Integer.parseInt(splitted_string[3]);

                    // No sé por qué se me volteó??
                    GameController.moles[c][r].setSelected(true);

                    GameController.progress_bar.setProgress((Integer.parseInt(ronda)+1) * .1);
                }

                /*
                if(GameController.should_smash){
                    smash(username, 4, 1, Integer.parseInt(randN),
                            Integer.parseInt(LobbiesController.current_lobby_socket_port));
                    GameController.should_smash = false;
                }*/

            } //TODO: manejar el final de ronda y seguir jugando

            messageConsumer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    void connectToLoginServer(){
        Socket socket = null;

        try{
            socket = new Socket(ipLogin, puertoLogin);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            lobbys = new ArrayList<String>();

            String data = in.readUTF();
            while(!data.equals("end_list")){
                lobbys.add(data);
                data = in.readUTF();
            }

            System.out.println("Recibí los siguientes lobbys:");
            System.out.println(lobbys.toString());

        } catch (UnknownHostException e) {
            System.out.println("Sock:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        } finally {
            if (socket != null) try {
                socket.close();
            } catch (IOException e) {
                System.out.println("close:" + e.getMessage());
            }
        }
    }

    public void smash(String id, int r, int c, int randN, int lobby_socket_port){
        Socket s = null;
        String score = "-1";
        try {
            int serverPort = lobby_socket_port;

            s = new Socket(ipLogin, serverPort);
            // s = new Socket("127.0.0.1", serverPort);
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            out.writeUTF(id+","+r+","+c+","+randN);            // UTF is a string encoding
            System.out.println("Tried to smash: "+id+","+r+","+c+","+randN);
            //score = in.readUTF();
        } catch (UnknownHostException e) {
            System.out.println("Sock:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        } finally {
            if (s != null) try {
                s.close();
            } catch (IOException e) {
                System.out.println("close:" + e.getMessage());
            }
        }
    }

    public static void main(String[] args){
        Cliente cliente = new Cliente("user1", 6970, "localHost");
        cliente.start();
    }

}
