#!/bin/bash
cd "docs/"
pandoc ./drafts/Informe.md -o Informe.pdf --toc
