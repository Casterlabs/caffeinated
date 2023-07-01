#!/bin/bash

echo "Building for Windows..."

JRE_DOWNLOAD_URL="https://api.adoptium.net/v3/binary/latest/11/ga/windows/x64/jre/hotspot/normal/eclipse?project=jdk"
MAIN_CLASS="co.casterlabs.caffeinated.updater.Launcher"

if [ ! -f windows_runtime.zip ]; then
    echo "Downloading JRE from ${JRE_DOWNLOAD_URL}."
    wget -O windows_runtime.zip $JRE_DOWNLOAD_URL
fi

java -jar "packr.jar" \
    --platform windows64 \
    --jdk windows_runtime.zip \
    --executable Casterlabs-Caffeinated-Updater \
    --classpath target/Casterlabs-Caffeinated-Updater.jar \
    --mainclass $MAIN_CLASS \
    --vmargs caffeinated.channel=stable \
    --output dist/windows

echo "Finished building for Windows."

cd dist/windows
zip -r ../artifacts/Windows.zip *
cd - # Return.
