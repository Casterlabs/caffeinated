#!/bin/bash


# (Optional) Compile everything
if [[ $@ != *"nocompile"* ]]; then
    # Delete old ui build stuff.
    rm -rf ui/build
    rm -rf app/Bootstrap/src/main/resources/app
    
    # Build UI
    cd ui
    npm i
    npm run build
    
    # Copy output to app/Bootstrap/src/main/resources/app
    cd ..
    cp -r ui/build app/Bootstrap/src/main/resources/app
    
    # Compile the maven project
	cd app
    mvn clean package
    cd ..
fi

# Reset/clear the dist folder
rm -rf dist/*
mkdir -p dist

KAIMEN_VERSION="08a84c3"

if [[ $@ != *"nodist"* ]]; then
    cp app/Bootstrap/target/classes/commit.txt dist

    echo ""
    echo "Completing packaging of application."
    echo ""

    java -jar ProjectBuilder.jar -os MACOSX -arch AMD64 -v 1.2 -n "Casterlabs-Caffeinated" -jv JAVA11 -kv $KAIMEN_VERSION -cp app/Bootstrap/target/Caffeinated.jar -i icon.icns -id co.casterlabs.caffeinated
    echo ""

    java -jar ProjectBuilder.jar -os WINDOWS -arch AMD64 -v 1.2 -n "Casterlabs-Caffeinated" -jv JAVA11 -kv $KAIMEN_VERSION -res app/WMC-JsonConsoleWrapper.exe -cp app/Bootstrap/target/Caffeinated.jar -i icon.ico
    echo ""

    java -jar ProjectBuilder.jar -os LINUX -arch AMD64 -v 1.2 -n "Casterlabs-Caffeinated" -jv JAVA11 -kv $KAIMEN_VERSION -cp app/Bootstrap/target/Caffeinated.jar
    echo ""

fi
