#!/bin/bash

filesToCheck=("./binary/tpeBinary.jar" "./docs/Informe.pdf" "./docs/Presentacion.pdf" "./src ./README.md")


	
for FILENAME in "${filesToCheck[@]}"
do
	if [ ! -f $FILENAME ]; then
		echo "No se encontro el archivo" $FILENAME
		exit 1;   
	fi
done
echo "Estan todos los archivos que se piden"
exit 0;
