# Whac-A-Mole

Implementación en Java del juego *[whac-a-mole](https://en.wikipedia.org/wiki/Whac-A-Mole)* multijugador usando topicos de [Active MQ](https://activemq.apache.org/) y [TCP sockets](https://www.ibm.com/docs/en/zvse/6.2?topic=SSB27H_6.2.0/fa2ti_what_is_socket_connection.htm).

Elaborado por Emilio Cantú y Pedro Olivares para la clase de Sistemas Distribuidos - ITAM Primavera 2023.

## Contenido
- [Cómo usar](#cómo-usar)
  - [Prerrequisitos:](#prerrequisitos)
  - [Jugar](#jugar)
  - [Desarrollar](#desarrollar)
- [ServidorLogin](#servidorlogin)
- [ServidorJuego](#servidorjuego)
- [Cliente gráfico](#cliente-gráfico)
- [Estresamiento](#estresamiento)


## Cómo usar
### Prerrequisitos:

- El proyecto fue desarrollado y probado con [JDK v19.0.1 de OpenJDK](https://openjdk.org/projects/jdk/19/) para ambos el cliente y el servidor por lo que
se recomienda [instalar](https://jdk.java.net/19/) la misma versión. 
- También se requiere [Active MQ v5.17.3](https://activemq.apache.org/activemq-5017003-release).
- Para la parte gráfica, [JavaFX v19](https://gluonhq.com/products/javafx/).

Hay un conflicto de librerías al utilizar JavaFX junto con ActiveMQ. Para corregirlo, excluir la librería "geronimo" de ActiveMQ.

### Jugar
Para jugar, se deben alzar el ServidorLogin y ServidorJuego. Una vez iniciados, se corre la clase LoginApplication (que se encuentra en Cliente.zip), la cual enseñara una pantalla en donde se pide el nombre del jugador, y el ip y puerto del ServidorLogin. Después de dar estos inputs, el cliente puede jugar con libertad en el lobby de su elección.

![image](https://user-images.githubusercontent.com/61219691/224865025-71288292-e061-4490-a835-ccb63a9227d5.png)


### Desarrollar
Para desarrollar sobre el proyecto recomendamos clonar este repositorio y usar [IntelliJ IDEA](https://www.jetbrains.com/idea/) como IDE.


## ServidorLogin
Servidor en los que los clientes consiguen la información necesaria para conectarse con los servidores de juego disponibles.

El servidor escucha un puerto designado y a cada conexión TCP que se recibe en el puerto le manda la información necesaria para conectarse a cada lobby. Se escribe una cadena UTF para cada lobby con formato:

`lobby,ip,puerto_amq,puerto_socket,r,c`

Donde:

- `lobby` es el nombre del lobby y del tópico AMQ por el que el ServidorJuego se comunica con los jugadores.
- `ip` es la dirección IPv4 del ServidorJuego.
- `puerto_amq` es el puerto donde vive ActiveMQ en el servidor.
- `puerto_socket` es el puerto por donde los clientes pueden hacer conexiones de TCP para comunicarse con el ServidorJuego.
- `r` es el número de renglones para la cuadrícula del juego.
- `c` es el número de columnas para la cuadrícula del juego.


El cliente entonces puede elegir entre los lobbys (servidores de juego) enviados y realizar la conexión directamente.

**Nota:** La implementación actual es solo apta para un número reducido de lobbys, que asumimos. Para una implementación donde se asumen un elevado número de lobbys se le puede asignar un lobby automáticamente a cada jugador. 

El ServidorLogin fue implementado como hilo y se le puede especificar el `puerto` TCP por el cual espera conexiones mediante el constructor y agregar, editar, y borrar lobbys con los metodos `agregar_lobby` , `modificar_lobby` y `borrar_lobby` respectivamente. 
 
## ServidorJuego
Servidor responsable de partidas jugadas en un lobby.

Se realizan partidas en un ciclo infinito mientras se reciban respuestas de los jugadores.
Para cada ronda de una partida el servidor manda, por el tópico AMQ, coordenadas al azar
junto con un número aleatorio que identifica a la ronda.

El primer jugador que
haga responda con los datos mandados junto con su username por el socket TCP designado es el ganador de la ronda.


Al final del número especificado de rondas se anuncia el ganador por el tópico AMQ.
Adicionalmente, el servidor se duerme una cantidad de tiempo especificada entre rondas y  partidas.

El ServidorJuego fue implementado como hilo y se le pueden especificar:
- `lobby` es el nombre del lobby y del tópico AMQ por el que el servidor se comunica con los jugadores.
- `tcp_port` el puerto TCP por donde se aceptan las respuestas de los jugadores.
- `n_rondas` el número de rondas por partida.
- `r` es el número de renglones para la cuadrícula del juego.
- `c` es el número de columnas para la cuadrícula del juego.
- `tiempo_entre_rondas` el tiempo entre rondas en milisegundos.
- `tiempo_entre_partidas` el tiempo entre partidas en milisegundos.

## Cliente gráfico

Para la parte gráfica se utilizó JavaFX. El código se encuentra en Cliente.zip.

El funcionamiento es con base en un hilo de clase "Cliente" que interactúa con ambos servidores y con las pantallas generadas por JavaFX.

El cliente consulta al ServidorLogin vía TCP para obtener toda la información de los lobbies, y con esta información generar las pantallas siguientes.

![image](https://user-images.githubusercontent.com/61219691/224861415-9478656d-bd10-456f-b0c1-e4b0986a1d76.png)

El cliente elige en qué lobby jugar:

![image](https://user-images.githubusercontent.com/61219691/224864339-ab00f0f8-9f98-446f-a69f-d36184e38216.png)

Y entra al lobby en donde se enseñan los topitos a los que se quiere golpear, consumidos vía activemq, después de hacer clic en el botón de start. Al golpear un topito, el cliente intenta establecer una conexión con el ServidorJuego para que se incremente su puntuación.

![image](https://user-images.githubusercontent.com/61219691/224864504-ca733e50-7f05-4a98-8532-8d99e9d7ea0d.png)

Al final de la partida, cuyo progreso se va desplegando en una barra de progreso en la esquina inferior derecha, se le informa a cada jugador el ganador de la partida vía terminal. 


## Estresamiento

La metodología y resultados del estresamiento de los servidores de login y juego se encuentran en [estresamiento.md](https://github.com/emiliocantuc/proyectoAlphaDistribuidos/blob/main/estresamiento.md).


