# Informe Caso 3 - Concurrencia y Sincronizacion de Procesos

**Integrantes**

- Alejandro Cruz Acevedo - 201912149
- Nicolas Castano Calderon - 202420324

## 1. Contexto

Este proyecto simula un sistema IoT para un campus universitario. Los eventos pasan por una cadena de actores concurrentes que producen, revisan, clasifican y consolidan informacion. El objetivo es aplicar sincronizacion en Java usando primitivas basicas y coordinar el cierre ordenado de todos los hilos.

## 2. Division del trabajo

El proyecto se trabajo en pareja y se dividio en dos bloques:

- Parte de entrada y filtrado: `Sensor`, `BuzonEntrada`, `Broker`, `BuzonAlertas` y `Administrador`.
- Parte de clasificacion y consolidacion: `BuzonClasificacion`, `Clasificador`, `BuzonConsolidacion`, `ServidorConsolidacionDespliegue` y `ControlFinClasificadores`.

Mi parte corresponde al segundo bloque. El punto de union entre ambas mitades es el `BuzonClasificacion`, que recibe eventos desde el `Broker` y desde el `Administrador` y los entrega a los clasificadores.

## 3. Estructura del sistema

### Clases principales

| Clase | Funcion |
|---|---|
| `Evento` | Guarda el id del evento, su tipo y si es un evento de fin. |
| `BuzonEntrada` | Cola compartida donde los sensores depositan eventos. |
| `BuzonAlertas` | Cola compartida donde el broker deja alertas para el administrador. |
| `BuzonClasificacion` | Buzon acotado `tam1` donde se reciben los eventos listos para clasificar. |
| `BuzonConsolidacion` | Buzon acotado `tam2` para cada servidor de consolidacion. |
| `Sensor` | Genera eventos y los deposita en `BuzonEntrada`. |
| `Broker` | Lee eventos de entrada y decide si van a alertas o a clasificacion. |
| `Administrador` | Revisa alertas y reenvia a clasificacion las que no son peligrosas. |
| `Clasificador` | Retira eventos de clasificacion y los envia al servidor correspondiente. |
| `ServidorConsolidacionDespliegue` | Procesa los eventos que llegan a su buzon. |
| `ControlFinClasificadores` | Coordina cual clasificador es el ultimo en terminar. |
| `Main` | Lee la configuracion, crea los objetos, arranca los hilos y espera su cierre. |

### Diagrama resumido

```text
Sensor -> BuzonEntrada -> Broker -> BuzonClasificacion -> Clasificador -> BuzonConsolidacion -> Servidor
                                   \-> BuzonAlertas -> Administrador ---/
```

## 4. Funcionamiento general

1. `Main` lee `config.txt` y obtiene `ni`, `base`, `nc`, `ns`, `tam1` y `tam2`.
2. Se calcula cuantos eventos debe procesar el sistema y se crea una `CyclicBarrier` para arrancar todos los hilos al mismo tiempo.
3. Cada `Sensor` genera `base * id` eventos y los deposita en `BuzonEntrada`.
4. El `Broker` retira eventos de entrada, genera un numero aleatorio entre 0 y 200 y decide si el evento es normal o una alerta.
5. Los eventos normales pasan a `BuzonClasificacion`; las alertas pasan a `BuzonAlertas`.
6. El `Administrador` revisa las alertas con espera semi-activa. Si una alerta resulta inofensiva, la envia a `BuzonClasificacion`. Si no, la descarta.
7. Cada `Clasificador` retira eventos de `BuzonClasificacion`, mira su tipo y los envia al `BuzonConsolidacion` correspondiente.
8. Cada `ServidorConsolidacionDespliegue` procesa los eventos de su buzon y espera el siguiente.
9. Cuando el `Broker` termina, envia un evento de fin al `Administrador`.
10. El `Administrador`, al recibir el fin, deposita un evento de fin por cada clasificador.
11. El ultimo clasificador en terminar envia un evento de fin a cada servidor de consolidacion.
12. `Main` hace `join()` sobre todos los hilos y termina cuando toda la simulacion ha finalizado.

## 5. Sincronizacion entre objetos

### `Sensor` y `BuzonEntrada`

Los sensores son productores y `BuzonEntrada` es un recurso compartido. El deposito es sincronizado con `synchronized` y el retiro usa `wait()` hasta que haya eventos disponibles. Cuando un sensor deposita, llama a `notifyAll()` para despertar al broker.

### `Broker` y `BuzonAlertas`

El broker deposita alertas en `BuzonAlertas`. Este buzon no bloquea al depositar, y el administrador lo lee con espera semi-activa usando `Thread.yield()` mientras la cola esta vacia.

### `Broker` y `BuzonClasificacion`

Cuando un evento es normal, el broker lo deposita en `BuzonClasificacion` con espera pasiva. Si el buzon esta lleno, espera con `wait()` hasta que un clasificador retire algun evento.

### `Administrador` y `BuzonClasificacion`

El administrador tambien puede enviar eventos a `BuzonClasificacion`, pero lo hace con espera semi-activa mediante `depositarSemiActivo()`. Si el buzon esta lleno, usa `Thread.yield()` hasta que haya espacio.

### `Clasificador` y `BuzonClasificacion`

Los clasificadores consumen eventos de `BuzonClasificacion` con espera pasiva. Si la cola esta vacia, esperan con `wait()` y se despiertan cuando llega un nuevo evento.

### `Clasificador` y `BuzonConsolidacion`

Cada clasificador actua como productor de un buzon de consolidacion. Si el buzon del servidor destino esta lleno, el clasificador espera con `wait()` hasta que el servidor retire eventos.

### `Clasificador` y `ControlFinClasificadores`

Cuando un clasificador recibe un evento de fin, llama a `ultimoEnTerminar()`. Este metodo esta sincronizado para evitar condiciones de carrera. Solo el ultimo clasificador que termina envia los eventos de fin a los servidores.

### `ServidorConsolidacionDespliegue` y `BuzonConsolidacion`

Cada servidor consume solo de su propio buzon. Si no hay eventos, espera con `wait()`. Cuando recibe un evento normal, lo procesa durante un tiempo aleatorio entre 100 ms y 1000 ms.

### `Main` y `CyclicBarrier`

`Main` crea una barrera de arranque con `CyclicBarrier` para que todos los hilos comiencen al mismo tiempo. Al final usa `join()` para esperar a que terminen sensores, broker, administrador, clasificadores y servidores.

## 6. Validacion

La validacion se hizo con el archivo `config.txt` incluido en el proyecto:

```text
ni=3
base=5
nc=5
ns=2
tam1=2
tam2=2
```

### Pruebas realizadas

- Se compilo el proyecto con `javac -d out src\\*.java`.
- Se ejecuto el programa con `java -cp out Main`.
- Se verifico que la simulacion terminara con el mensaje `Simulacion completada.`
- Se reviso que los sensores terminaran, que el broker procesara todos los eventos esperados, que el administrador enviara los eventos de fin, y que clasificadores y servidores cerraran correctamente.

### Resultado observado

Con esa configuracion, el sistema funciono sin bloqueos y cerro de forma ordenada. La salida del programa mostro el paso de eventos entre sensores, broker, administrador, clasificadores y servidores, hasta la terminacion completa de la simulacion.

## 7. Conclusion

La implementacion cubre la arquitectura completa del Caso 3 y usa sincronizacion de forma coherente en cada etapa. Los buzones acotados controlan el flujo de eventos, `wait()` y `notifyAll()` resuelven la espera pasiva, `Thread.yield()` cubre la espera semi-activa y `CyclicBarrier` mas `join()` permiten coordinar el arranque y el cierre del sistema.

Mi parte queda centrada en la clasificacion y consolidacion de eventos, y se integra con la parte de mi companero a traves de `BuzonClasificacion`.
