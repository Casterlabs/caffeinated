#!/bin/bash

set -e
set -o pipefail

# (Optional) Compile everything
if [[ $@ != *"nocompile"* ]]; then
    cd app
    ./mvnw clean package
    cd ..

    # Compile the deploy helper maven project
    cd deploy-helper
    ./mvnw clean package
    cd ..
fi

# Reset/clear the dist folder
rm -rf dist/*
mkdir -p -p dist

KAIMEN_VERSION="37f6491"
VM_OPTIONS="-vm Xms1M -vm XX:+UseCompressedOops -vm XX:+UseSerialGC -vm XX:MaxHeapFreeRatio=1 -vm XX:MinHeapFreeRatio=1"

if [[ $@ != *"nodist"* ]]; then
    cp app/Bootstrap/target/classes/commit.txt dist
    mkdir dist/artifacts

    echo ""
    echo "Completing packaging of application."
    echo ""

    java -jar ProjectBuilder.jar $VM_OPTIONS -os MACOSX -arch AMD64 -wi WEBKIT -v 1.2 -n "Casterlabs-Caffeinated" -jv JAVA11 -kv $KAIMEN_VERSION -cp app/Bootstrap/target/Caffeinated.jar -i icon.icns -id co.casterlabs.caffeinated
    cd dist/macOS-amd64
    zip -r ../artifacts/macOS-amd64.zip *
    rm -rf ./jre
    zip -r ../artifacts/macOS-amd64-nojre.zip *
    cd -
    echo ""

    java -jar ProjectBuilder.jar $VM_OPTIONS -os WINDOWS -arch AMD64 -wi CHROMIUM_EMBEDDED_FRAMEWORK -v 1.2 -n "Casterlabs-Caffeinated" -jv JAVA11 -kv $KAIMEN_VERSION -res app/WMC-JsonConsoleWrapper.exe -cp app/Bootstrap/target/Caffeinated.jar -i icon.ico
    cd dist/Windows-amd64
    zip -r ../artifacts/Windows-amd64.zip *
    rm -rf ./jre
    zip -r ../artifacts/Windows-amd64-nojre.zip *
    cd -
    echo ""

    java -jar ProjectBuilder.jar $VM_OPTIONS -os LINUX -arch AMD64 -wi CHROMIUM_EMBEDDED_FRAMEWORK -v 1.2 -n "Casterlabs-Caffeinated" -jv JAVA11 -kv $KAIMEN_VERSION -cp app/Bootstrap/target/Caffeinated.jar
    cd dist/Linux-amd64
    zip -r ../artifacts/Linux-amd64.zip *
    rm -rf ./jre
    zip -r ../artifacts/Linux-amd64-nojre.zip *
    cd -
    echo ""

fi
