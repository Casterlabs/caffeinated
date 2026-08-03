#!/bin/bash

set -e -o pipefail

APP_ID="co.casterlabs.caffeinated"
APP_NAME="Casterlabs-Caffeinated"
MAIN_CLASS="co.casterlabs.caffeinated.bootstrap.Bootstrap"
VM_OPTIONS='--arg=-Xms1M --arg=-XX:+UseCompressedOops --arg=-XX:MaxHeapFreeRatio=2 --arg=-XX:MinHeapFreeRatio=1'
SAUCER4J_VERSION="1a278f1"
JCEF_VERSION="146.0.10"
JCEF_NATIVES_VERSION="jcef-d3de827+cef-146.0.10+g8219561+chromium-146.0.7680.179"

if [[ $@ == *"compile"* ]]; then
    echo "------------ Compiling app ------------"

    cd app
    bash ./mvnw clean package
    cd ..

    echo "------------ Finishing compiling app ---------"
fi

if [[ $@ == *"dist-windows"* ]]; then
    echo "------------ Bundling for Windows ------------"

    java -jar bundler.jar bundle \
        --arch x86_64 --os windows \
        --id $APP_ID --name $APP_NAME --icon icon.png \
        --java 11 $VM_OPTIONS --main $MAIN_CLASS \
        --sign 'cmd.exe /C C:\signing\sign.bat Casterlabs-Caffeinated.exe' \
        --file app/core/WMC-JsonConsoleWrapper.exe --dependency "app/core/target/Caffeinated.jar" \
        --dependency "https://repo1.maven.org/maven2|me.friwi:jcef-natives-windows-amd64:$JCEF_NATIVES_VERSION:.jar"
        #--dependency "https://jitpack.io|com.github.saucer.saucer4j:webview2:$SAUCER4J_VERSION:.jar"

    echo "------------ Finished bundling for Windows ------------"
fi

if [[ $@ == *"dist-nosign-windows"* ]]; then
    echo "------------ Bundling for Windows ------------"

    java -jar bundler.jar bundle \
        --arch x86_64 --os windows \
        --id $APP_ID --name $APP_NAME --icon icon.png \
        --java 11 $VM_OPTIONS --main $MAIN_CLASS \
        --file app/core/WMC-JsonConsoleWrapper.exe --dependency "app/core/target/Caffeinated.jar" \
        --dependency "https://repo1.maven.org/maven2|me.friwi:jcef-natives-windows-amd64:$JCEF_NATIVES_VERSION:.jar"
        #--dependency "https://jitpack.io|com.github.saucer.saucer4j:webview2:$SAUCER4J_VERSION:.jar"

    echo "------------ Finished bundling for Windows ------------"
fi

if [[ $@ == *"dist-macos"* ]]; then
    echo "------------ Bundling for macOS ------------"

    java -jar bundler.jar bundle \
        --arch aarch64 --os macos \
        --id $APP_ID --name $APP_NAME --icon icon.png \
        --java 11 --arg=-XstartOnFirstThread $VM_OPTIONS --main $MAIN_CLASS \
        --dependency "app/core/target/Caffeinated.jar" \
        --dependency "https://jitpack.io|com.github.saucer.saucer4j:webkit:$SAUCER4J_VERSION:.jar"

    java -jar bundler.jar bundle \
        --arch x86_64 --os macos \
        --id $APP_ID --name $APP_NAME --icon icon.png \
        --java 11 --arg=-XstartOnFirstThread $VM_OPTIONS --main $MAIN_CLASS \
        --dependency "app/core/target/Caffeinated.jar" \
        --dependency "https://jitpack.io|com.github.saucer.saucer4j:webkit:$SAUCER4J_VERSION:.jar"

    echo "------------ Finished bundling for macOS ------------"
fi

if [[ $@ == *"dist-linux"* ]]; then
    echo "------------ Bundling for Linux ------------"

    java -jar bundler.jar bundle \
        --arch aarch64 --os gnulinux \
        --id $APP_ID --name $APP_NAME --icon icon.png \
        --java 11 $VM_OPTIONS --main $MAIN_CLASS \
        --dependency "app/core/target/Caffeinated.jar" \
        --dependency "https://jitpack.io|com.github.saucer.saucer4j:webkitgtk:$SAUCER4J_VERSION:.jar"

#    java -jar bundler.jar bundle \
#        --arch arm --os gnulinux \
#        --id $APP_ID --name $APP_NAME --icon icon.png \
#        --java 11 $VM_OPTIONS --main $MAIN_CLASS \
#        --dependency "app/core/target/Caffeinated.jar" \
#        --dependency "https://jitpack.io|com.github.saucer.saucer4j:webkitgtk:$SAUCER4J_VERSION:.jar"

    java -jar bundler.jar bundle \
        --arch x86_64 --os gnulinux \
        --id $APP_ID --name $APP_NAME --icon icon.png \
        --java 11 $VM_OPTIONS --main $MAIN_CLASS \
        --dependency "app/core/target/Caffeinated.jar" \
        --dependency "https://jitpack.io|com.github.saucer.saucer4j:webkitgtk:$SAUCER4J_VERSION:.jar"

    echo "------------ Finished bundling for Linux ------------"
fi

