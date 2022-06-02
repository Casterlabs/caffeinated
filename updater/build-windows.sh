#!/bin/bash


echo "Building for Windows..."

JRE_DOWNLOAD_URL="https://api.adoptium.net/v3/binary/version/jdk-11.0.13%2B8/windows/x64/jre/hotspot/normal/eclipse?project=jdk"
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
     --output dist/windows

echo "Finished building for Windows."
