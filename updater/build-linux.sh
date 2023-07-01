#!/bin/bash

# Let's get it started.
echo "Building for Linux..."

JRE_DOWNLOAD_URL="https://api.adoptium.net/v3/binary/latest/11/ga/linux/x64/jre/hotspot/normal/eclipse?project=jdk"
MAIN_CLASS="co.casterlabs.caffeinated.updater.Launcher"

if [ ! -f linux_runtime.tar.gz ]; then
    echo "Downloading JRE from ${JRE_DOWNLOAD_URL}."
    wget -O linux_runtime.tar.gz $JRE_DOWNLOAD_URL
fi

java -jar "packr.jar" \
    --platform linux64 \
    --jdk linux_runtime.tar.gz \
    --executable Casterlabs-Caffeinated-Updater \
    --classpath target/Casterlabs-Caffeinated-Updater.jar \
    --mainclass $MAIN_CLASS \
    --vmargs caffeinated.channel=stable \
    --output dist/linux

echo "Finished building for Linux."

cd dist/linux
tar -czvf ../artifacts/Linux.tar.gz *
cd -
