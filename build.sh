#!/bin/bash

set -euxo pipefail

echo "Build channel: ${DEPLOY_CHANNEL:-default}"

if [[ $@ != *"nocompile"* ]]; then
    cd app
    bash ./mvnw clean package
    cd ..
fi

# Reset/clear the dist folder
rm -rf dist/*
mkdir -p -p dist

KAIMEN_VERSION="3cb3816"
VM_OPTIONS="-vm Xms1M -vm XX:+UseCompressedOops -vm XX:+UseSerialGC -vm XX:MaxHeapFreeRatio=1 -vm XX:MinHeapFreeRatio=1"

if [[ $@ != *"nopackage"* ]]; then
    cp app/App/target/classes/commit.txt dist
    mkdir dist/artifacts

    echo ""
    echo "Completing packaging of application."
    echo ""

    java -jar ProjectBuilder.jar $VM_OPTIONS -os MACOS -arch X86 -archWord 64 -wi WEBKIT -v 1.2 -n "Casterlabs-Caffeinated" -jv JAVA11 -kv $KAIMEN_VERSION -cp app/App/target/Caffeinated.jar -i icon.icns -id co.casterlabs.caffeinated
    cd dist/macOS-x86_64
    zip -r ../artifacts/macOS-amd64.zip *
    rm -rf ./jre
    zip -r ../artifacts/macOS-amd64-nojre.zip *
    cd -
    echo ""

    java -jar ProjectBuilder.jar $VM_OPTIONS -os WINDOWS_NT -arch X86 -archWord 64 -wi CHROMIUM_EMBEDDED_FRAMEWORK -v 1.2 -n "Casterlabs-Caffeinated" -jv JAVA11 -kv $KAIMEN_VERSION -res app/WMC-JsonConsoleWrapper.exe -cp app/App/target/Caffeinated.jar -i icon.ico
    cd dist/Windows-x86_64
    zip -r ../artifacts/Windows-amd64.zip *
    rm -rf ./jre
    zip -r ../artifacts/Windows-amd64-nojre.zip *
    cd -
    echo ""

    java -jar ProjectBuilder.jar $VM_OPTIONS -os LINUX -arch X86 -archWord 64 -wi CHROMIUM_EMBEDDED_FRAMEWORK -v 1.2 -n "Casterlabs-Caffeinated" -jv JAVA11 -kv $KAIMEN_VERSION -cp app/App/target/Caffeinated.jar
    cd dist/Linux-x86_64
    zip -r ../artifacts/Linux-amd64.zip *
    rm -rf ./jre
    zip -r ../artifacts/Linux-amd64-nojre.zip *
    cd -
    echo ""

fi
