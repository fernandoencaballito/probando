#!/bin/bash
set -ev
mvn clean package
chmod 700 ./target/uber-tp-0.0.1-SNAPSHOT.jar
sudo timeout 20s java -jar ./target/uber-tp-0.0.1-SNAPSHOT.jar localhost 110 localhost
exit(0)
