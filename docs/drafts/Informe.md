% Trabajo práctico especial
% Alumnos: Agustin Prado; Fernando Bejarano
% Fecha de entrega: Miércoles 10 de Junio

# Decisión de diseño implementadas

Uso de patrones
------------------------


Se opto por el uso del patron reactor para la implementacion del maneja de los eventos.
Se tiene una clase que implemeta este patron e itera por las claves del selector , permitiendo asi despachar a cada manejador correspondiente segun la operacion requerida (accept,connect ,write ,read).Estos ultimos dos eventos pasa por una clase que funciona como Parser y permite su intercepcion y alteracion del contenido en caso correspondiente




# Descripción detallada de los protocolos y aplicaciones desarrolladas

Protocolo administrador
------------------------

Las líneas enviadas tanto por el cliente como las respondidas por el servidor, deben
terminar con la secuencia CRLF.  
La máxima longitud de línea soportado es de 512 bytes. Si se envía una línea de
mayor longitud, los bytes que se excedan de este tamaño son descartados.  
Este protocolo es sensible a mayúsculas, por lo tanto no es lo mismo emplear el
comando “QUIT” (comando correcto)  que emplear el comando “quit”(comando incorrecto).

Este protocolo admite una única contraseña que por defecto es “password”. Si se desea cambiar esta contraseña, se debe modificar el valor "ADMIN_PASSWORD" en el archivo de propiedades del proxy server ("proxyServer.properties") ; en
futuros desarrollos se podría agregar la posibilidad de emplear múltiples contraseñas y/o
logueo de distintos usuarios administradores.

Para iniciar una sesión de este protocolo se debe conectar por consola a la ip del servidor proxy y utilizar como puerto aquel especificado para este protocolo. Por defecto, el puerto para este protocolo es 10001; sin embargo el mismo puede ser modificado desde el archivo de propiedades, cambiando la propiedad "ADMIN_PORT".

Los comandos que se pueden emplear son los siguientes:

* PASS: especificación de la password para autenticarse.
* MET1: permite obtener la cantidad de accesos hechos al servidor proxy.
* MET2: permite obtener la cantidad de bytes transferidos por el servidor proxy.
* SET: permite especificar un servidor origin a un determinado usuario.
* TOFF: deshabilitar la transformación de los mensajes provenientes de los clientes.
* TON: habilitar la transformación de mensajes provenientes de los clientes.
* MON: habilitar multiplexación de cuentas.
* MOFF: deshabilitar multiplexación de cuentas.
* QUIT: comando para terminar la sesión.
* MUTEON: comando para silenciar los mensajes de un usuario en particular.
* MUTEOFF: comando para permitir que un usuario vuelva a enviar mensajes, es decir que revierte los efectos de silenciado. Se indica un mensaje de error en caso de que el usuario especificado nunca se haya silenciado.



Una sesión consta de dos partes: primero una fase de autenticación y luego una fase de transacción. Para pasar a la segunda fase, es necesario autenticarse con la contraseña correspondiente. Durante la segunda fase, se puede ejecutar los comandos para acceder a estadísticas y administrar el servidor proxy. En cualquiera de las dos fases, se puede cerrar la conexión empleando el comando "QUIT".


En el siguiente ejemplo se ve una sesión de este protocolo. En la misma se emplean todos los comandos. Las lineas que empiezan con "S:" son las que envía el servidor; las que empiezan con "C:" son las que le envía el cliente administrador al servidor.

    S: +OK password required to administrate the server
    
    C: PASS password
    S: +OK ready
    
    C: MET1
    S: +OK xx.

    C: MET2
    S: +OK xx.

    C: SET user origin originPort
    S: +OK

    C: MON
    S: +OK accounts multiplexing is on

    C: MOFF
    S: +OK accounts multiplexing is off

    C: TOFF
    S: +OK transformation of the subject is off

    C: TON
    S: +OK transformation of the subject is on

    C: MUTEON user
    S: +OK user has been muted.

    C: MUTEOFF user
    S: +OK

    C: QUIT
    S: +OK

La especificación de la sintaxis de invocación de cada comando en formato ABNF (rfc 5234) es la siguiente:

    PASS    = "PASS" SP adminPassword CRLF;
    MET1    = "MET1" CRLF;
    MET2    = "MET2" CRLF;
    SET    = "SET" SP user SP originServer SP originPort CRLF;
    TON    = "TON" CRLF;
    TOFF    = "TOFF" CRLF;
    MON    = "MON" CRLF;
    MOFF    = "MOFF" CRLF;
    QUIT    = "QUIT" CRLF;
    MUTEON    = "MUTEON" SP user CRLF;
    MUTEOFF    = "MUTEOFF" SP user CRLF;
    adminPassword    = ALPHA ;
    originPort     = *DIGIT; Debe ser un puerto tcp válido.
    originServer    = host;
    host: se encuentra definido en  rfc2396.
    user: esta definido exactamente como el elemento "localpart" en el rfc 6122.

Como respuesta de los comando se puede obtener dos tipos de respuestas: "+OK" o "-ERR" en forma similar al protocolo pop3.
Para el caso los comandos "MET1" Y "MET2" el formato de la respuesta exitosa es la siguiente en formato abnf:
    
    RESPUESTA_MET    ="+OK" SP métrica CRLF;
    métrica     = *DIGIT; valor de la métrica.
    
    
Dado que el nombre de usuario se encuentra en codificacion UTF-8, todas las líneas que se reciben se interpretan en este formato.


Parsers del protocolo XMPP
--------------------------

Para el caso en el que un usuario silenciado trate de enviar un mensaje y para el caso en el que el servidor origin trate de pasarle un
mensaje a un usuario silenciado, se procede a responderle con un mensaje de error a la parte que esta mandando el mensaje. Se decidió emplear el
mensaje de error que figura en el rfc 6120 punto  "8.3.3.10" denominado "not-allowed".Se decidió usar este tipo de mensaje de error para
indicar que el servidor no permite enviar el mensaje desde o hacia un usuario silenciado.



Parseo Stanzas
--------------------------

El parseo de las Stanzas se realiza según la llegada de los elementos(tags) de la misma.
Al iniciar una conexión el primer tag valido posible es del tipo <?xml > , mediante un listener y el uso de la librería AALTO se procede a validar que los demas tags siguientes estén bien formados y sean válidos ,en casos de ser inválidas se envía una correspondiente Stanza informando al cliente.
En caso de estar activado el  comando para alterar los mensajes , se procede a primero chequear que el usuario no esté silenciado y luego se intercepta todo el contenido dentro del tag <body></body> para su conversión a formato l33t.
En caso de estar silenciado el usuario se enviará una Stanza de error informando al cliente y no se procesarán sus mensajes.
Al recibir una Stanza del tipo <si > (las que que indican transferencia de archivo) , esta se deja sin alterar y se permite la comunicación entre cliente-servidor para el envío de archivos(dejamos la negociación y el transfer del mensaje a cargo del servidor).


# Problemas encontrados durante el diseño y la implementación

Durante las fases de diseño, se notó la complejidad que se deriva del parseo de streams xml.Inicialmente, se opto por utilizar librerías
que implementan la interfaz STAX. El problema de las mismas fue que solo permitian parsear un conjunto de datos fijos, no stream de datos
como se necesita para este trabajo. Luego de investigar, se resolvio utilizar la librería "faster xml - Aalto" para el parseo ya que la misma.




# Limitaciones de la aplicación
* Solo permite autenticación plana
* No soporta encriptación
* No soporta compresión

# Posibles extensiones

# Conclusiones

# Ejemplos de testeo

# Guia de instalación detallada y precisa.  

La siguiente guía de instalación es válida para el sistema operativo Ubuntu 6.10 o
versiones superiores.
Los pasos a seguir para la instalación son los siguientes:

1. Para este trabajo practico se requiere la instalacion de java 8.
En caso de que se lo tenga que instalar, utilizar en consola los siguientes comandos
$ sudo add-apt-repository ppa:webupd8team/java
$ sudo apt-get update
$ sudo apt-get install oracle-java8-installer

2. Instalar apache maven.
3. Se debe situar en la raíz del proyecto (donde se encuentra el archivo "pom.xml") y se
debe ejecutar el el comando "mvn package". Este comando del programa Maven crea
un jar ejecutable de nombre uber-tp-0.0.1-SNAPSHOT.jar  dentro de la carpeta target
(en la raíz del proyecto).

# Instrucciones para la configuración.

# Ejemplos de configuración y monitoreo.

# Documento de diseño del proyecto (que ayuden a entender la arquitectura de la aplicación)

 ![arquitectura](drafts/arquitectura.jpg "arquitectura")

* Modulo administrador
* Modulo XMPP
* Modulo de métricas
* Modulo "AdminModule"

# Pruebas de stress
* cuál es la máxima cantidad de conexiones simultáneas que soporta?
* cómo se degrada el throughput?
