package co.casterlabs.caffeinatedapi;

import java.io.IOException;

import co.casterlabs.sora.SoraFramework;
import co.casterlabs.sora.SoraLauncher;

public class Test {

    public static void main(String[] args) throws IOException {
        SoraFramework framework = new SoraLauncher()
            .buildWithoutPluginLoader();

        framework
            .getSora()
            .register(new CaffeinatedApi());

        framework.startHttpServer();
    }

}
