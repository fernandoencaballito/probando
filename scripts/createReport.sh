#!/bin/bash
cd "docs/"
pandoc ./drafts/Informe.md -o Informe.pdf --toc
 aspell list --lang=es < ./drafts/Informe.md
echo 'Creado el informe'

