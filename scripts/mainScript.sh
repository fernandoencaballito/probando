#!/bin/bash
ls
chmod 700 ./scripts/*
./scripts/compileProject.sh
./scripts/smokeTest.sh
./scripts/createReport.sh
