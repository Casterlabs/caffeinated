#!/bin/bash

MAIN_CLASS="co.casterlabs.caffeinated.updater.Launcher"

# (Optional) Compile everything
if [[ $@ != *"nocompile"* ]]; then
    mvn clean package
fi

# Reset/clear the dist folder
rm -rf dist/*
mkdir -p dist
mkdir dist/artifacts
mkdir dist/windows
mkdir dist/linux
mkdir dist/macos
mkdir dist/macos/Casterlabs-Caffeinated.app

if [[ $@ != *"nodist"* ]]; then
    # Windows
    sh build-windows.sh
    echo ""

    # Linux
    sh build-linux.sh
    echo ""

    # MacOS
    sh build-macos.sh
    echo ""
fi
