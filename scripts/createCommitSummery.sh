#!/bin/bash
rm ./docs/caratulaCommit
cat ./docs/caratula_parte1 >> ./docs/caratulaCommit 
git fetch
git rev-parse origin/master >> ./docs/caratulaCommit
cat ./docs/caratula_parte2 >> ./docs/caratulaCommit
echo 'antes bol'

