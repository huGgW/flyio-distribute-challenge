#!/bin/bash

if [[ $BASH_SOURCE = */* ]]; then
    DIR=${BASH_SOURCE%/*}/
else
    DIR=./
fi

# make shadowJar if not exists
if [ ! -f "$DIR/build/libs/app-all.jar" ]; then
    echo "Building the application..."
    $DIR/gradlew shadowJar
fi

# run the application
java -jar $DIR/build/libs/app-all.jar
