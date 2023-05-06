package itam.mx.packages.alpha_version_final;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Servidor en los que los clientes consiguen informacion del servidor de juego.
 * A cada conexion TCP que se hace en el puerto se escribe la informacion del servidor de juego
 * para cada lobby. El formato de la informacion escrita es:
 * nombre de lobby, ip, puerto_amq, puerto_socket, no. de renglones (r),no. de columnas (c).
 * El cliente despues puede elegir entre los lobbys enviados y realizar la conexion directamente.
 */
public class ServidorLogin extends Thread{

    // Puerto donde escucha ServidorLogin
    int puerto;

    // Cadena que indica el final de la lista de lobbys a mandar
    String END_LIST;

    // Dict. con la informacion de lobbys. nombre de lobby -> dict. de info.
    private HashMap<String,HashMap<String,String>> lobbys;

    // Arreglo de cadenas correspondiente a lobbys a mandar.
    private String[] lobbys_info;

    public ServidorLogin(int puerto) {
        this.puerto = puerto;
        this.lobbys = new HashMap<String,HashMap<String,String>>();
        this.lobbys_info = null;
        this.END_LIST = "end_list";
    }

    /**
     * Dada la info. de un lobby, lo agrega al registro.
     * @param nombre Nombre del lobby.
     * @param ip Dir. IP del ServidorJuego del lobby.
     * @param puerto_amq Puerto del queue de Active MQ por el que el servidor manda coordenadas.
     * @param puerto_socket Puerto del socket por el que el servidor acepta respuestras de clientes.
     * @param r No. de renglones para la cuadricula del juego.
     * @param c No. de columnas para la cuadricula del juego.
     */
    public void agregar_lobby(String nombre, String ip,String puerto_amq,String puerto_socket,String r, String c){
        HashMap<String, String> info = new HashMap<String,String>();
        info.put("ip",ip);
        info.put("puerto_amq",puerto_amq);
        info.put("puerto_socket",puerto_socket);
        info.put("r",r);
        info.put("c",c);
        this.lobbys.put(nombre,info);
        this.lobbys_info = get_lobbys_info();

    }

    /**
     * Borra el lobby con 'nombre' del registro
     * @param nombre Nombre del lobby a borrar.
     */
    public void borrar_lobby(String nombre){
        this.lobbys.remove(nombre);
        this.lobbys_info = get_lobbys_info();

    }

    /**
     * Reemplaza el atributo 'campo' del lobby 'nombre' con 'nuevo_valor'
     * @param nombre Nombre del lobby.
     * @param campo Campo de la info. del lobby a reemplazar.
     * @param nuevo_valor Valor con la que reemplazar el campo.
     */
    public void modificar_lobby(String nombre,String campo, String nuevo_valor){
        this.lobbys.get(nombre).replace(campo,nuevo_valor);
        this.lobbys_info = get_lobbys_info();
    }

    /**
     * Regresa la representacion en cadenas de la informacioin de los lobbys.
     * El formato para la info. de cada lobby que se regresa es:
     * nombre de lobby, ip, puerto_amq, puerto_socket, no. de renglones (r),no. de columnas (c).
     * @return Arreglo con una entrada por lobby con su info separada por comas.
     */
    private String[] get_lobbys_info(){
        String keys[] = {"ip","puerto_amq","puerto_socket","r","c"};
        String values[] = new String[keys.length+1];
        String out[] = new String[this.lobbys.size()];
        HashMap<String,String> info;
        int i = 0;
        for(Map.Entry<String, HashMap<String,String>> entry : this.lobbys.entrySet()){
            info = entry.getValue();
            values[0] = entry.getKey();
            for(int k = 1;k < values.length;k++)
                values[k] = info.get(keys[k-1]);
            out[i] = String.join(",",values);
            i++;
        }
        return out;
    }

    /**
     * Metodo principal que corre el hilo.
     * Espera conexiones de TCP en this.puerto y manda un mensaje UTF para cada lobby
     * en el registro despues de aceptar una conexion. Despues indica que ha terminado
     * de mandar la lista y cierra la conexion.
     */
    @Override
    public void run(){
        // Declaramos sockets y data streams
        ServerSocket serverSocket;
        DataOutputStream out;
        Socket socket;

        // Intentamos hacer binding en puerto
        try {
            serverSocket = new ServerSocket(this.puerto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Y mientras no se mate el hilo
        while(true) {
            try {
                // Aceptamos conexion en el puerto
                System.out.println("Accepting on port "+this.puerto+"...");
                socket = serverSocket.accept();
                // Construimos un output steam
                out = new DataOutputStream(socket.getOutputStream());
                // Mandamos la info de cada lobby como cadena UTF
                for(String lobby_info : this.lobbys_info)
                    out.writeUTF(lobby_info);
                // Mandamos la indicacion de que la lista ha terminado
                out.writeUTF(this.END_LIST);
                // Para depurar
                System.out.println("Se sirvio a "+socket.getInetAddress());
                // Y cerramos la conexion
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main (String args[]){
        // Inicializamos servidor
        ServidorLogin s = new ServidorLogin(6970);
        // Le agregamos lobbys
        s.agregar_lobby("Lobby 1","localhost","61614","6971","2","2");
        s.agregar_lobby("Lobby 2","localhost","61614","6972","3","3");
        s.agregar_lobby("Lobby 3","localhost","61614","6973","4","4");
        // Y lo levantamos
        s.start();
    }

}

/*
class Main {
    public static void main (String args[]){
        // Inicializamos servidor
        ServidorLogin s = new ServidorLogin(6970);
        // Le agregamos lobbys
        s.agregar_lobby("lobby1","localhost","61614","6971","8","8");
        s.agregar_lobby("lobby2","localhost","61614","6972","10","10");
        s.agregar_lobby("lobby3","localhost","61614","6973","14","14");
        // Y lo levantamos
        s.start();
    }
}
 */
