#!/bin/bash
defaultJarPath="./target/uber-tp-0.0.1-SNAPSHOT.jar"
finalJarPath="./binary/tpeBinary.jar"
configurationFile="./proxyServer.properties"
finalPath="./binary/"
mvn clean package
#echo $defaultJarPath $finalJarPath
mkdir binary
cp $defaultJarPath $finalJarPath
cp $configurationFile $finalPath
