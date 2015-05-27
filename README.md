tpe-protos[![Build Status](https://travis-ci.org/fernandoencaballito/probando.svg?branch=master)](https://travis-ci.org/fernandoencaballito/probando)
=========

Estructura del repositorio
--------------------------

├── binary 	//Contiene el archivo binario ejecutable del trabajo
├── docs	//Contiene el informe y la presentación.
│   ├── drafts	//Contiene los borradores del informe y la carátula del trabajo ( la que se incluye en el sobre).
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
│   	├── documentsDependencies.sh //Script que instala los programas necesarios para convertir el informe a pdf.
├── src				//Archivos fuente del trabajo
│   ├── main			
│   │     	
│   └── test			//Archivos fuente para realizar testing.


Nota sobre el directorio "binary"
------------------------
En este directorio ya se provee un binario ejecutable segun se solicita en el informe.Dentro de dicha carpeta se encuentra copiado el archivo de propiedades "proxyServer.properties".

Compilacion del programa
------------------------
Se debe situar en la raiz del proyecto (donde se encuentra el archivo "pom.xml") y se debe ejecutar el el comando "mvn package". Este comando del programa Maven crea un jar ejecutable de nombre "uber-tp-0.0.1-SNAPSHOT.jar" dentro de la carpeta target (en la raiz del proyecto).

Ejecucion del programa
----------------------
Para ejecutar el programa se puede simplemente ejecutar el jar que se obtiene luego del paso anterior mediante el comando "sudo  java -jar uber-tp-0.0.1-SNAPSHOT.jar". En el mismo directorio que contenga el ejecutable debe estar el archivo "proxyServer.properties" (se encuentra en la raiz del proyecto); opcionalmente se le puede pasar como parámetro la ubicación del archivo de configuración de propiedades. 
Ejemplo: ""sudo  java -jar uber-tp-0.0.1-SNAPSHOT.jar ./proxyServer.properties" 
