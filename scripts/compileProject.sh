#!/bin/bash
defaultJarPath="./target/uber-tp-0.0.1-SNAPSHOT.jar"
finalJarPath="./binary/tpeBinary.jar"
configurationFolder="properties/"
finalPath="./binary/"
mvn clean package
#echo $defaultJarPath $finalJarPath
rm -rf binary
mkdir binary
cp $defaultJarPath $finalJarPath
cp -r $configurationFolder $finalPath
