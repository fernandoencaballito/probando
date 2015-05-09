% Trabajo práctico especial 
% Alumnos: Agustin Prado; Pablo Pauli ; Fernando Bejarano 
% Fecha de entrega: Miércoles 10 de Junio 

# Descripción detallada de los protocolos y aplicaciones desarrolladas

Protocolo administrador
connected to the administration of the pop3 proxy server
S: +OK password required to administrate the server
C: PASS password
S: +OK ready
C: MET1
S: xx.
C: MET2
S: xx.
C: SET user originUrl
S: +OK
C: MON
S: +OK accounts multiplexing is on
C: MOFF
S: +OK accounts multiplexing is off
C: TOFF
S: +OK transformation of the subject is off
C: TON
S: +OK transformation of the subject is on
C: QUIT
S: +OK


# Problemas encontrados durante el diseño y la implementación

# Limitaciones de la aplicación

# Posibles extensiones

# Conclusiones

# Ejemplos de testeo

# Guia de instalación detallada y precisa.  

# Instrucciones para la configuración.

# Ejemplos de configuración y monitoreo.

# Documento de diseño del proyecto (que ayuden a entender la arquitectura de la aplicación)

 ![arquitectura](drafts/arquitectura.jpg "arquitectura")
Modulo administrador
Modulo XMPP
Modulo de metricas
Modulo "AdminModule"



# Pruebas de stress
* cuál es la máxima cantidad de conexiones simultáneas que soporta?
* cómo se degrada el throughput?
