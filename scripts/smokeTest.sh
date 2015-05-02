#!/bin/bash
cleanup(){
previousExitCode=$?
if [ $previousExitCode -eq 124 ]; then
	echo 'Paso la smokeTest.sh'
	 exit 0
fi
echo 'ERROR: FALLA EN LA smokeTest.sh'
exit $previousExitCode
}

jarPath="./target/uber-tp-0.0.1-SNAPSHOT.jar"
set -ev
chmod 700 $jarPath
trap  "cleanup" EXIT
timeout 20s java -jar $jarPath ./proxyServer.properties

