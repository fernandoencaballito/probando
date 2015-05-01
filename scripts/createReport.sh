#!/bin/bash
cd "docs/"
pandoc ./drafts/Informe.md -o Informe.pdf --toc
echo '________________________________________________________'
echo 'Revisando errores de ortografía en el informe'
 aspell list --lang=es < ./drafts/Informe.md
echo '----Creado el informe'

