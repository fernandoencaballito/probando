#!/bin/bash
defaultJarPath="./target/uber-tp-0.0.1-SNAPSHOT.jar"
finalJarPath="./binary/tpeBinary.jar"
mvn clean package
#echo $defaultJarPath $finalJarPath
cp $defaultJarPath $finalJarPath
