tpe-protos[![Build Status](https://travis-ci.org/fernandoencaballito/probando.svg?branch=master)](https://travis-ci.org/fernandoencaballito/probando)
=========

Estructura del repositorio
--------------------------

├── binary 	//Contiene el archivo binario ejecutable del trabajo
├── docs	//Contiene el informe y la presentación.
│   ├── drafts	//Contiene los borradores del informe y la carátula del trabajo ( la que se incluye en el sobre).
│   ├── documentsDependencies.sh //Script que instala los programas necesarios para convertir el informe a pdf.
├── pom.xml	//archivo de maven
├── README.md		
├── scripts	//scripts empleados durante la fase de desarrollo.No es necesario correrlos para el correcto funcionamiento del trabajo.
│   ├── checkRequiredFiles.sh	//Revisa si estan los archivos requiridos en el enunciado.
│   ├── compileProject.sh	//Compilación del trabajo.Deja el ejecutable en la carpeta "binary".
│   ├── createCommitSummery.sh	//Crea e imprime la carátula que se adjunta al sobre de la entrega del trabajo.
│   ├── createReport.sh		//Convierte el informe a formato pdf y lo deja en la carpeta "docs".
│   ├── mainScript.sh		//Script principal. Utiliza a los otros scripts para compilar y testear el trabajo.
│   ├── smokeTest.sh		//Prueba el archivo ejecutable
│   └──install	//Programas que se instalaron durante la fase de desarrollo.
├── src				//Archivos fuente del trabajo
│   ├── main			
│   │     	
│   └── test			//Archivos fuente para realizar testing.


Compilacion del programa
------------------------


Ejecucion del programa
----------------------



Servicio
----------
 ultimo, los comandos para iniciar y parar el servicio desde consola son los siguientes:"sudo start pop3proxy" y "sudo stop pop3proxy".



Protocolo de administrador
-------------------------
Este protocolo solo admite la contraseña "password".
