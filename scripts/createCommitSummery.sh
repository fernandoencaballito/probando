#!/bin/bash
fileName='./docs/caratulaCommit'
rm  $fileName
cat ./docs/caratula_parte1 >> $fileName 
git fetch
git rev-parse origin/master >> $fileName
cat ./docs/caratula_parte2 >> $fileName
echo 'archivo armado'
lpr $fileName
echo 'archivo enviado a imprimir'

