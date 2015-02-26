#!/bin/bash
part1= cat ./docs/caratula_parte1
cod= git rev-parse origin/master
part2= cat ./docs/caratula_parte2
$part1 $cod $part2 > ./docs/caratulaCommit
 echo 'pepe' > joder.txt
