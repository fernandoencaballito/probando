#!/bin/bash
cleanup(){
previousExitCode=$?
if [ $previousExitCode -eq 124 ]; then
	 exit 0
fi
exit $previousExitCode
}

jarPath="./target/uber-tp-0.0.1-SNAPSHOT.jar"
set -ev
mvn clean package
chmod 700 $jarPath
trap  "cleanup" EXIT
sudo timeout 20s java -jar $jarPath localhost 110 localhost

