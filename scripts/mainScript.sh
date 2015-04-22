#!/bin/bash
ls
chmod 700 ./scripts/compileProject.sh
./scripts/compileProject.sh
chmod 700 ./scripts/smokeTest.sh
./scripts/smokeTest.sh
