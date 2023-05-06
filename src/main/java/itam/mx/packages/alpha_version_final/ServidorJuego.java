package itam.mx.packages.alpha_version_final;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * TODO: acepta puerto para aqm
 */
public class ServidorJuego extends Thread {

    // El puerto en el que escucha el servidor, el no. de rondas, renglones, columnas
    // en para la cuadricula del juego y el tiempo que se espera una respuesta de clientes
    // antes de volver a enviar coordenadas por el topico.
    private int tcp_port, n_rondas, matriz_r, matriz_c,SO_TIME;

    // El nombre del lobby y cadena que indica final de partida
    private String lobby, BYE;

    // El tiempo entre rondas y partidas respectivamente, en milisegundos.
    private long tiempo_entre_rondas,tiempo_entre_partidas;

    // El diccionario de la forma username -> # de puntos
    private HashMap<String,Integer> resultados;

    // URL del servidor AMQ a usar
    private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;

    public ServidorJuego(int tcp_port,String lobby, int n_rondas, int matriz_r, int matriz_c, String BYE, long tiempo_entre_rondas,long tiempo_entre_partidas) {
        this.tcp_port = tcp_port;
        this.lobby = lobby;
        this.n_rondas = n_rondas;
        this.matriz_r = matriz_r;
        this.matriz_c = matriz_c;
        this.BYE = BYE;
        this.tiempo_entre_rondas = tiempo_entre_rondas;
        this.tiempo_entre_partidas = tiempo_entre_partidas;

        // Tiempo a esperar por respuesta antes de mandar coordenadas de nuevo
        this.SO_TIME=5000;
    }

    /**
     * Manda msg por el topico this.lobby usando la conexion AMQ 'connection'.
     * @param connection Connection abierta de ActiveMQConnectionFactory.
     * @param msg Mensaje a enviar por el topico this.topico de 'connection'.
     */
    private void sendMsg(Connection connection, String msg) {
        MessageProducer messageProducer;
        TextMessage textMessage;
        try {
            // Creamos una sesion y creamos/definimos que queremos usar topico this.lobby.
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(this.lobby);

            // Creamos un productor de mensaje de texto con el mensaje.
            messageProducer = session.createProducer(destination);
            textMessage = session.createTextMessage();
            textMessage.setText(msg);

            // Mandamos el mensaje
            messageProducer.send(textMessage);

            // Cerramos producto y sesion.
            messageProducer.close();
            session.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * Espera y procesa respuesta de clientes/jugadores. El primero en conectarse
     * al TCP socket y mandar las coordenadas r, c y el no. aleatorio correspondiente
     * a la ronda se regresa como ganador.
     * Si el cliente manda r, c, randN incorrectos o el mensaje es mal formateado
     * se cierra la conexion y espera otra.
     * El formato de mensaje que se espera es: username, r_in, c_in, randN_in.
     * El TCP socket se abre en puerto this.tcp_port.
     * @param r La coordenada de renglones que mando el servidor.
     * @param c La coordenada de columnas que mando el servidor.
     * @param randN El no. aleatorio que mando el servidor.
     * @return El username del ganador.
     * @throws IOException
     */
    private String get_respuesta(int r,int c,int randN) throws IOException {
        // Incializamos sockets y data stream.
        ServerSocket serverSocket = null;
        Socket socket = null;
        DataInputStream in = null;
        DataOutputStream out = null;

        // Si ha ocurrido timeout y no se aceptaron conexiones en this.SO_TIME desde
        // que se abrio el socket.
        boolean timeout=false;

        // El id del ganador y mensaje recibido.
        String id_ganador = null, msg = null;
        // El mensaje separado por comas
        String splited_msg[];

        int r_in = -1,c_in = -1,randN_in = -1;

        // Mientras r, c, y randN que manda el cliente no coincidan con las mandadas
        // y mientras no ocurra timeout:
        while((r_in!=r || c_in!=c || randN_in!=randN) && !timeout){
            System.out.println("Esperando respuesta en TCP Socket "+this.tcp_port+" ...");
            try {
                // Aceptamos conexiones en this.tcp_port con timeout this.SO_TIME.
                serverSocket = new ServerSocket(this.tcp_port);
                serverSocket.setSoTimeout(this.SO_TIME);
                socket = serverSocket.accept();
                System.out.println("Conexion aceptada de "+socket.getInetAddress().toString());
                // Creamos data stream para recibir
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
            }catch(SocketTimeoutException e){
                System.out.println("Timeout. No se encontraron respuestas...");
                timeout=true;
            }

            try {
                // Si se acepto la conexion y creo el data stream:
                if (in!=null) {
                    // Leemos el mensaje como cadena con formato username, r_in, c_in, randN_in:
                    msg = in.readUTF();
                    splited_msg = msg.split(",");
                    id_ganador = splited_msg[0];
                    r_in = Integer.parseInt(splited_msg[1]);
                    c_in = Integer.parseInt(splited_msg[2]);
                    randN_in = Integer.parseInt(splited_msg[3]);
                }
            }
            catch(IOException e){
                System.out.println("Mal formato de mensaje. Closing connection...");
                id_ganador = null;
            }

            // Cerramos socket y datastream
            System.out.println("Cerrando socket...");
            if(in!=null) in.close();
            if(socket!=null) socket.close();
            if(serverSocket!=null) serverSocket.close();

            System.out.println("Recibimos respuesta: " + msg);
        }

        // Regresamos al ganador de la ronda
        System.out.println("Ganador de ronda: "+id_ganador);
        return id_ganador;
    }



    /**
     * Metodo principal que corre el hilo.
     * Ciclo infinito. Para cada partida, se completan this.n_rondas.
     * Para cada ronda se mandan, por el topico AMQ, coordenadas al azar junto con un numero aleatorio
     * que identifica a la ronda. El cliente/jugador que mande hago echo a los datos (junto
     * con su username) es el ganador de la ronda. Al final de las n rondas se anuncia el
     * ganador por el topico AMQ. Se duerme this.tiempo_entre_rondas y this.tiempo_entre_partidas
     * milisegundos entre rondas y partidas respectivamente.
     *
     */
    @Override
    public void run() {
        System.out.println("Starting server...");

        Random rand = new Random();
        int r = -1, c = -1, randN = -1;
        String ganador_ronda;

        try {
            while(true) {
                // Para transmitir con colas de ActiveMQ
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
                Connection connection = connectionFactory.createConnection();
                connection.start();
                System.out.println("Started connection at URL: " + url);


                // Reseteamos puntos
                this.resultados = new HashMap<String,Integer>();

                // Para cada ronda:
                for (int ronda = 0; ronda < this.n_rondas; ronda++) {

                    // Reseteamos el ganador de ronda
                    ganador_ronda=null;

                    // Generamos no. al azar para identificar ronda
                    randN = Math.abs(rand.nextInt());

                    // Generamos coordenadas al azar
                    r = rand.nextInt(this.matriz_r);
                    c = rand.nextInt(this.matriz_c);

                    // Construimos el msg a enviar sperado por comas
                    String msg = ronda + "," + randN + "," + r + "," + c;

                    // Mientras no se encuentre ganador de la ronda
                    while(ganador_ronda==null){
                        // Mandamos el mensaje
                        sendMsg(connection, msg);
                        System.out.println("Mandando mensaje : " + msg);
                        // Y esperamos respuesta de ganador
                        ganador_ronda = get_respuesta(r, c, randN);
                    }

                    // Guardamos ganador
                    Integer v = resultados.containsKey(ganador_ronda) ? resultados.get(ganador_ronda) : Integer.valueOf(0);
                    resultados.put(ganador_ronda, v + 1);


                    // Dormimos entre rondas
                    if(ronda<this.n_rondas-1)
                        Thread.sleep(this.tiempo_entre_rondas);
                }

                // Mandamos bye/ganador
                sendMsg(connection, this.BYE + "," + String.join(",", sortByValue(this.resultados)));

                // Dorminos entre partidas
                Thread.sleep(this.tiempo_entre_partidas);

                // Cerramos la conexion AMQ
                connection.close();
            }
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            System.out.println("No se pudo abrir TCP Socket");
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * Ordena las llaves de 'map' por sus valores y los regresa en una lista.
     * @param map Mapa del tipo K -> V a ordenar.
     * @return Lista de de llaves ordenadas.
     * @param <K> Tipo de datos de llaves del mapa.
     * @param <V> Tipo de datos de valores del mapa.
     */
    public static <K, V extends Comparable<? super V>> List<K> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        List<K> result=new LinkedList<>();
        for (Map.Entry<K, V> entry : list) {
            result.add(entry.getKey());
        }
        return result;
    }


    public static void main(String[] args) {
            System.out.println("Lanzando un servidor...");
            // Inicializamos y lanzamos un servidor de prueba
            // Lobby 1
            new ServidorJuego(
                    6971,
                    "Lobby 1",
                    10,
                    2,
                    2,
                    "bye",
                    1000,
                    5000
            ).start();

            // Lobby 2
            new ServidorJuego(
                    6972,
                    "Lobby 2",
                    10,
                    3,
                    3,
                    "bye",
                    1000,
                    5000
            ).start();

        // Lobby 3
            new ServidorJuego(
                    6973,
                    "Lobby 3",
                    10,
                    4,
                    4,
                    "bye",
                    1000,
                    5000
            ).start();
    }
}
