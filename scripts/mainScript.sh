#!/bin/bash
#scriptsToRun=("./scripts/compileProject.sh" "./scripts/smokeTest.sh" "./scripts/createReport.sh" "./scripts/checkRequiredFiles.sh")

scriptsToRun=("./scripts/compileProject.sh" "./scripts/smokeTest.sh" "./scripts/createReport.sh" )

chmod 700 ./scripts/*


for SCRIPTNAME in "${scriptsToRun[@]}"
do
		$SCRIPTNAME
		previousExitCode=$?
		if [ $previousExitCode -ne 0 ]; then
			echo 'Falla en el script:' $SCRIPTNAME
	 		exit $previousExitCode
		fi
done

echo 'MainScript:TODO BIEN'
exit 0
#./scripts/compileProject.sh
#./scripts/smokeTest.sh
#./scripts/createReport.sh
