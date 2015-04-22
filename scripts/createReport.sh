#!/bin/bash
cd "docs/"
pandoc ./drafts/Informe.md -o Informe.pdf --toc
echo 'Creado el informe'
