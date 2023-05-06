import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class EstresadorJuego extends Thread{

    String lobby,ip,username;

    int juega_n_partidas,puerto_tcp;

    public EstresadorJuego(String lobby, String ip, String username, int juega_n_partidas, int puerto_tcp) {
        this.lobby = lobby;
        this.ip = ip;
        this.username = username;
        this.juega_n_partidas = juega_n_partidas;
        this.puerto_tcp = puerto_tcp;
    }

    @Override
    public void run(){

        String received,r,c,randN;
        String[] splitted_string;

        String url = ActiveMQConnection.DEFAULT_BROKER_URL;
        try{
            MessageConsumer messageConsumer;
            TextMessage textMessage;
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
            Connection connection = connectionFactory.createConnection();
            connection.start();

            // Intentamos escuchar en el topico de lobby
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(this.lobby);
            messageConsumer = session.createConsumer(topic);
            connection.start();

            int partidas_jugadas = 1;

            while(partidas_jugadas < juega_n_partidas){
                // Juega
                textMessage = (TextMessage) messageConsumer.receive();
                received = textMessage.getText();

                //Splittea los datos
                splitted_string = received.split(",");
                if(splitted_string.length == 4){
                    randN = splitted_string[1];
                    r = splitted_string[2];
                    c = splitted_string[3];
                    smash(this.username+","+r+","+c+","+randN);
                }else{
                    partidas_jugadas++;
                }
            }
            messageConsumer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public void smash(String msg){
        Socket s = null;
        try {
            s = new Socket(this.ip, this.puerto_tcp);
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            out.writeUTF(msg);            // UTF is a string encoding
        } catch (UnknownHostException e) {
            //System.out.println("Sock:" + e.getMessage());
        } catch (EOFException e) {
            //System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            //System.out.println("IO:" + e.getMessage());
        } finally {
            if (s != null) try {
                s.close();
            } catch (IOException e) {
                //System.out.println("close:" + e.getMessage());
            }
        }
    }

    public static void main(String[] args){
        new EstresadorJuego("lobby1","localhost","1",10,6972).start();
    }


}
