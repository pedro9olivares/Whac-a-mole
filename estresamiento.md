
# Proyecto $\alpha$ - Estresamiento

Elaborado por Emilio Cantú y Pedro Olivares para la clase de Sistemas Distribuidos - ITAM Primavera 2023.

Detallamos la metodología y resultados del estresamiento de los servidores de login y de juego.


## ServidorLogin
Recordamos cómo opera el servidor:

El servidor escucha un puerto designado y a cada conexión TCP que se recibe en el puerto le manda la información necesaria para conectarse a cada lobby. Se escribe una cadena UTF para cada lobby con la información necesaria para conectarse a cada uno.

Estresamos al servidor simulando clientes que se conectan al servidor y midiendo el tiempo de respuesta, desde que se abre la conexión hasta que se termina de recibir la lista de cadenas UTF con información de los lobbys. 


Simulamos diferente número de clientes (100, 200, 300, 400, 500, 600, 700, 800, 900, 1000) y distintas cantidades de conexiones al servidor por cliente (100, 200, 300, 400, 500, 600, 700, 800, 900, 1000). Realizamos el producto cruz entre las variables y 10 simulaciones por configuración para presentar el promedio.


Medimos el tiempo de respuesta:

![](/imgs_estresamiento/login-10-avg.png)

Y notamos, con excepción de las corridas con 100 hilos, que entre más hilos mayor es el tiempo de respuesta promedio. Para cada número de hilos observamos que el tiempo de respuesta promedio se mantiene aproximadamente constante relativo al número de requests por hilo. Especulamos que esto se debe a que los requests que cada cliente hace requests secuencialmente y por ello el número de requests en tránsito es constante. También especulamos que la latencia promedio elevada de las corridas con 100 hilos se debe a que son las primeras en realizarse y que el servidor todavía no termina de alzarse (o que el sistema operativo todavía no le asigna los suficientes recursos). 

La desviación estándar de los tiempos de respuesta:

![](/imgs_estresamiento/login-10-std.png)

Sigue básicamente el mismo patrón que el tiempo de respuesta promedio por lo que concluimos lo mismo. Entre más hilos hagan peticiones simultáneamente, más saturado se vuelve el servidor y más inconsistentes sus tiempos de respuesta.

Y la proporción de conexiones que resultan en error:

![](/imgs_estresamiento/login-10-error.png)

Donde observamos que, en general, entre mayor número de hilos, mayor es la proporción de errores que reciben los clientes. Esto es porque entre más hilos intentan hacer conexión con el socket, mayor es la probabilidad que el socket esté ocupado y se rechace la conexión. 

Como nuestro juego soporta la creación de varios lobbys, y el servidor de login manda una cadena de información por lobby, especulamos que el desempeño empeora con el número de lobbys. 

Para probarlo hacemos las mismas corridas realizadas arriba pero para servidores de login con 10, 100 y 1000 lobbys.

Medimos el tiempo de respuesta promedio:


![](/imgs_estresamiento/login-lobbys-avg.png)

La desviacion estandar de los tiempos de respuesta:


![](/imgs_estresamiento/login-lobbys-std.png)

Y el porcentaje de conexiones que terminan en error:


![](/imgs_estresamiento/login-lobbys-error.png)


Notamos que el desempeño del servidor empeora con el número de lobbys disponibles. Esto es porque entre más lobbys, más cadenas tiene que mandar el servidor por conexión (y así cada conexión tarda más) y se vuelve más saturado por lo que la variación entre los tiempos de respuesta aumenta y la probabilidad de que el puerto está ocupado también, que resulta en más conexiones rechazadas.


Como comentamos en el README.md, optamos por esta implementación porque asumimos un número pequeño de lobbys. Para una implementación donde se asumen un elevado número de lobbys se le puede mandar un lobby (o un número constante) automáticamente a cada jugador, en lugar de todos. 

## ServidorJuego

Recordamos cómo opera el servidor:

Se realizan partidas en un ciclo infinito mientras se reciban respuestas de los jugadores.
Para cada ronda de una partida el servidor manda, por el tópico AMQ, coordenadas al azar
junto con un número aleatorio que identifica a la ronda.

El primer jugador que
haga responda con los datos mandados junto con su username por el socket TCP designado es el ganador de la ronda.

Para estresar el servidor, entonces, simulamos n jugadores en distintos hilos y medimos el tiempo entre que el servidor manda coordenadas en AMQ y que recibe una respuesta de algún jugador (que llamamos "latencia" abajo). 

Medimos la latencia promedio variando el número de hilos/jugadores:

![](/imgs_estresamiento/juego-avg.png)

Notamos, como se esperaba, que la latencia promedio aumenta con el número que hilos. 

También medimos la desviación estándar de los tiempos de latencia:

![](/imgs_estresamiento/juego-std.png)

Notamos que también aumenta con el número de hilos. Esto es porque entre más hilos jugadores se intentan conectar con el servidor, más se satura y más se tarda en procesar los requests.
