tp-protos
=========

Compilacion del programa
------------------------
Se debe situar en la raiz del proyecto (donde se encuentra el archivo "pom.xml") y se debe ejecutar el el comando "mvn package". Este comando del programa Maven crea un jar ejecutable de nombre "uber-tp-0.0.1-SNAPSHOT.jar" dentro de la carpeta target (en la raiz del proyecto).



Ejecucion del programa
----------------------
Para ejecutar el programa se puede simplemente ejecutar el jar que se obtiene luego del paso anterior mediante el comando "sudo  java -jar uber-tp-0.0.1-SNAPSHOT.jar <origin-server> <port> <proxy-address>"; donde " <origin-server>" es la ip del origin server, <port> es el puerto del origin server y "<proxy-address>" es la ip de la maquina en la que se esta corriendo el servidor proxy. Si en cambio se lo desea instalar y correr como servicio, proceder al siguiente parrafo.
Nota: no ejecutar el jar "tp-0.0.1-SNAPSHOT.jar", emplear el jar "uber-tp-0.0.1-SNAPSHOT.jar; este ultimo contiene todas las librerias necesarias para que funcione el tp y el primero no.



Servicio
----------
Para que  nuestro trabajo funcione como  servicio correctamente, copiar el archivo "pop3proxy.conf" ( se encuentra en la raiz del projecto)  en el directorio "/etc/init".Luego modificar el archivo "pop3proxy.conf" recien copiado, cambiando el valor que tiene asignada la variable JARPATH por la ubicacion absoluta en la que se encuentra el archivo jar que se obtiene al compilar el proyecto en maven.Si se desea que escuche peticiones a la ip de la maquina en la que se lo esta corriendo, se puede cambiar la variable "LOCAL_ADDRESS" al valor de la ip de la misma.Por defecto escucha en la direccion "localhost" 
Por ultimo, los comandos para iniciar y parar el servicio desde consola son los siguientes:"sudo start pop3proxy" y "sudo stop pop3proxy".


POP3 Proxy
------------------
Los logs se encuentran en /var/log/proxyPOP3.log


Protocolo de administrador
-------------------------
Este protocolo solo admite la contraseña "password".