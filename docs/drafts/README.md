Compilacion del programa
------------------------
Se debe situar en la raiz del proyecto (donde se encuentra el archivo "pom.xml") y se debe ejecutar el el comando "mvn package". Este comando del programa Maven crea un jar ejecutable de nombre "proxyXMPP-0.0.1-SNAPSHOT.jar" dentro de la carpeta target (en la raiz del proyecto).

Ejecucion del programa
----------------------
Para ejecutar el programa se puede simplemente ejecutar el jar que se obtiene luego del paso anterior mediante el comando "sudo  java -jar proxyXMPP-0.0.1-SNAPSHOT.jar.

Desde el Psi hay que entrar en Modify Account en la cuenta que se quiera conectar al proxy, seleccionar configuracion de cuentas , elegir cuenta  a utilizar y seleccionar modificar.Enla pestaña conexion seleccionar editar donde dice configuracion proxy . Donde dice Host: escribir la ip del host que ejecuta el proxy y en puerto usar el puerto donde el proxy escuchara. Si se selecciona el puerto 10001 se entra como administrador.


## fernando fijate que como me dijiste que los puertos estan en  properties entonces no los especifico a la hora de ejecutar el jar.