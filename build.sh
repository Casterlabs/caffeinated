#!/bin/bash

# (Optional) Compile everything
if [[ $@ != *"nocompile"* ]]; then
    # Delete any old builds.
    rm -rf ui/build
    rm -rf widgets/static/loader
    rm -rf app/Bootstrap/src/main/resources/app
    rm -rf app/LocalServer/src/main/resources/loader
    rm -rf app/BuiltInPlugins/src/main/resources/widgets

    # Build UI
    cd ui
    npm i
    npm run build

    # Copy UI output to app/Bootstrap/src/main/resources/app
    cd ..
    mkdir -p app/Bootstrap/src/main/resources/app
    cd ui/build
    cp -r * ../../app/Bootstrap/src/main/resources/app
    cd ../..

    # Build Widgets
    cd widgets
    npm i
    npm run build

    # Copy Widgets output to app/BuiltInPlugins/src/main/resources/widgets
    cd ..
    mkdir -p app/BuiltInPlugins/src/main/resources/widgets
    cd widgets/build
    cp -r * ../../app/BuiltInPlugins/src/main/resources/widgets
    cd ../..

    # Copy the widget loader
    mkdir -p app/LocalServer/src/main/resources/loader
    cp -r widget-loader/* app/LocalServer/src/main/resources/loader

    # Compile the maven project
    cd app
    mvn clean package
    cd ..
fi

# Reset/clear the dist folder
rm -rf dist/*
mkdir -p -p dist

KAIMEN_VERSION="7257f2e12c"
VM_OPTIONS="-vm XX:+UnlockExperimentalVMOptions -vm XX:+UseShenandoahGC -vm XX:ShenandoahUncommitDelay=500 -vm XX:ShenandoahGuaranteedGCInterval=2000 -vm XX:ShenandoahGCHeuristics=compact"

if [[ $@ != *"nodist"* ]]; then
    cp app/Bootstrap/target/classes/commit.txt dist
    mkdir artifacts

    echo ""
    echo "Completing packaging of application."
    echo ""

    java -jar ProjectBuilder.jar $VM_OPTIONS -os MACOSX -arch AMD64 -wi WEBKIT -v 1.2 -n "Casterlabs-Caffeinated" -jv JAVA11 -kv $KAIMEN_VERSION -cp app/Bootstrap/target/Caffeinated.jar -i icon.icns -id co.casterlabs.caffeinated
    cd dist/macOS-amd64
    zip -r ../../artifacts/macOS-amd64.zip *
    rm -rf ./jre
    zip -r ../artifacts/macOS-amd64-nojre.zip *
    cd -
    echo ""

    java -jar ProjectBuilder.jar $VM_OPTIONS -os WINDOWS -arch AMD64 -wi CHROMIUM_EMBEDDED_FRAMEWORK -v 1.2 -n "Casterlabs-Caffeinated" -jv JAVA11 -kv $KAIMEN_VERSION -res app/WMC-JsonConsoleWrapper.exe -cp app/Bootstrap/target/Caffeinated.jar -i icon.ico
    cd dist/Windows-amd64
    zip -r ../../artifacts/Windows-amd64.zip *
    rm -rf ./jre
    zip -r ../artifacts/Windows-amd64-nojre.zip *
    cd -
    echo ""

    java -jar ProjectBuilder.jar $VM_OPTIONS -os LINUX -arch AMD64 -wi CHROMIUM_EMBEDDED_FRAMEWORK -v 1.2 -n "Casterlabs-Caffeinated" -jv JAVA11 -kv $KAIMEN_VERSION -cp app/Bootstrap/target/Caffeinated.jar
    cd dist/Linux-amd64
    zip -r ../../artifacts/Linux-amd64.zip *
    rm -rf ./jre
    zip -r ../artifacts/Linux-amd64-nojre.zip *
    cd -
    echo ""

fi
