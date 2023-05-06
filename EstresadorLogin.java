import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class EstresadorLogin extends Thread{

    String ip,id_corrida,END_LIST;
    int puerto,n_requests;

    public EstresadorLogin(String ip, int puerto,int n_requests,String id_corrida) {
        this.ip = ip;
        this.puerto = puerto;
        this.n_requests = n_requests;
        this.id_corrida = id_corrida;
        this.END_LIST = "end_list";
    }

    @Override
    public void run(){
        // Nombre del hilo
        String name = Thread.currentThread().getName();

        // Declaramos sockets y data streams
        DataInputStream in;
        Socket socket = null;

        // String recibido del servidor de login
        String msg = "";

        // Tiempos
        long tiempos[] = new long[this.n_requests];

        long start_time, time_taken;

        // Intentamos hacer binding en puerto
        try {
            // Establecemos TCP Socket
            socket = new Socket(this.ip, this.puerto);
            in = new DataInputStream(socket.getInputStream());

            // Para cada request
            for (int i = 0; i < this.n_requests; i++) {
                start_time = System.nanoTime();

                while (!msg.equals(this.END_LIST))
                    msg = in.readUTF();

                time_taken = System.nanoTime() - start_time;
                tiempos[i] = time_taken;
            }
            System.out.println(name + "," + promedio(tiempos) + "," + stdDev(tiempos) + "," + this.id_corrida);

        } catch (UnknownHostException e) {
            //System.out.println("Sock:" + e.getMessage());
            System.out.println(name + ",error,Sock," + this.id_corrida);
        } catch (EOFException e) {
            //System.out.println("EOF:" + e.getMessage());
            System.out.println(name + ",error,EOF," + this.id_corrida);
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
            System.out.println(name + ",error,IO," + this.id_corrida);
        } finally {
            if (socket != null) try {
                socket.close();
            } catch (IOException e) {
                //System.out.println("close:" + e.getMessage());
            }
        }


    }
    // Functiones para calcular estadisticas
    private double promedio(long[] list){
        double sum=0.0;
        for(int i=0;i<list.length;i++)
            sum+=list[i];
        if(list.length==0)
            return 0.0;
        return sum/list.length;
    }

    private double stdDev(long[] list) {
        double sum = 0.0;
        double num = 0.0;
        for (int i = 0; i < list.length; i++)
            sum += list[i];
        double mean = sum / list.length;
        for (int i = 0; i < list.length; i++)
            num += Math.pow((list[i] - mean), 2);
        return Math.sqrt(num / list.length);
    }

}
