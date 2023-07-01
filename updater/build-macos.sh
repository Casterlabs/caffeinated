#!/bin/bash

# Let's get it started.
echo "Building for MacOS..."

JRE_DOWNLOAD_URL="https://api.adoptium.net/v3/binary/latest/11/ga/mac/x64/jre/hotspot/normal/eclipse?project=jdk"
MAIN_CLASS="co.casterlabs.caffeinated.updater.Launcher"

if [ ! -f macos_runtime.tar.gz ]; then
    echo "Downloading JRE from ${JRE_DOWNLOAD_URL}."
    wget -O macos_runtime.tar.gz $JRE_DOWNLOAD_URL
fi

java -jar "packr.jar" \
    --platform mac \
    --jdk macos_runtime.tar.gz \
    --executable Casterlabs-Caffeinated-Updater \
    --icon app_icon.icns \
    --bundle co.casterlabs.caffeinated \
    --classpath target/Casterlabs-Caffeinated-Updater.jar \
    --mainclass $MAIN_CLASS \
    --vmargs caffeinated.channel=stable \
    --output dist/macos/Casterlabs-Caffeinated.app

echo "Finished building for MacOS."

cd dist/macos
tar -czvf ../artifacts/macOS.tar.gz *
cd - # Return.
