public class Estresador {

    public static void lanza_hilos_login(int n_hilos,int n_solicitudes,String id_corrida){
        EstresadorLogin[] hilos = new EstresadorLogin[n_hilos];

        // Inicializamos hilos
        for(int i = 0;i < n_hilos;i++)
            hilos[i] = new EstresadorLogin("localhost",6970,n_solicitudes,id_corrida);

        // Los lanzamos
        for(EstresadorLogin hilo : hilos)
            hilo.start();

    }

    public static void lanza_hilos_juego(String lobby,int n_hilos,int n_partidas){
        EstresadorJuego[] hilos = new EstresadorJuego[n_hilos];

        // Inicializamos hilos
        for(int i = 0;i < n_hilos;i++)
            hilos[i] = new EstresadorJuego(lobby,"localhost",""+i,n_partidas,6972);

        // Los lanzamos
        for(EstresadorJuego hilo : hilos)
            hilo.start();

    }
    public static void main(String[] args){
        String opcion = "juego";
        int n = 1;

        if(args.length==2){
            opcion = args[0];
            n = Integer.parseInt(args[1]);

        }

        if(opcion.equals("login")) {
            System.out.println("Estresando login con "+n+" lobbys");
            // Lanzamos servidor de login
            ServidorLogin s = new ServidorLogin(6970);
            // Le agregamos lobbys
            for(int i = 0;i < n;i++)
                s.agregar_lobby(""+i,"localhost","123456","123456","10","10");
            // Y lo levantamos
            s.start();
            // Y esperamos a que se levante
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Lanzamos corridas para el producto cruz de numClientes y numSolicitudes
            int[] numClientes = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};
            int[] numSolicitudes = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};

            // Cuantas veces corremos cada configuracion (numClientes,numSolicitudes)
            int n_lanzas_x_config = 10;
            try {
                for (int n_hilos : numClientes) {
                    for (int n_solicitudes : numSolicitudes) {
                        for (int i = 0; i < n_lanzas_x_config; i++) {
                            lanza_hilos_login(n_hilos, n_solicitudes, n_hilos + "-" + n_solicitudes);
                            Thread.sleep(500);

                        }
                        Thread.sleep(1000);
                    }
                }
            }catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }else if(opcion.equals("juego")){
            System.out.println("Lanzando un servidor de juego...");
            int n_lanzas_x_config = 10;
            // Inicializamos y lanzamos un servidor de prueba
            new ServidorJuego(
                    "lobby1",
                    6972,
                    100,
                    8,
                    8,
                    "bye",
                    0,
                    0,
                    true
            ).start();
            lanza_hilos_juego("lobby1",n,n_lanzas_x_config);
        }
    }
}
