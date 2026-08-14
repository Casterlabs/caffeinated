package co.casterlabs.caffeinated.window.jcef;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.CefSettings.LogSeverity;
import org.cef.callback.CefSchemeRegistrar;

import app.saucer.ntv.util.SaucerResourceUtil;
import co.casterlabs.commons.io.streams.StreamUtil;
import lombok.SneakyThrows;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.EnumProgress;
import me.friwi.jcefmaven.IProgressHandler;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;

class _CefUtil {
    public static final File bundleDirectory = new File("cef_bundle");

    public static final boolean ENABLE_OSR = false;

    static {
        try {
            CefAppBuilder builder = new CefAppBuilder();

            builder.addJcefArgs(
                "--disable-http-cache",
                "--disable-web-security", // Gross workaround for CORS issues between custom schemes and localhost.
                "--autoplay-policy=no-user-gesture-required"
            );
            builder.setInstallDir(bundleDirectory);
            builder.setProgressHandler(new IProgressHandler() {
                @Override
                public void handleProgress(EnumProgress state, float percent) {}
            });

            CefSettings settings = builder.getCefSettings();

            settings.background_color = settings.new ColorType(255, 0, 0, 0);
            settings.windowless_rendering_enabled = ENABLE_OSR;
            settings.log_severity = LogSeverity.LOGSEVERITY_DISABLE;

            builder.setAppHandler(new MavenCefAppHandlerAdapter() {
                @Override
                public void onRegisterCustomSchemes(CefSchemeRegistrar registrar) {
                    registrar.addCustomScheme(
                        "app",
                        true,   // standard
                        false,  // local
                        false,  // display isolated
                        true,   // secure
                        true,   // CORS enabled
                        false,  // CSP bypass
                        true    // fetch enabled?
                    );
                }

                @Override
                public void onContextInitialized() {
                    CefApp.getInstance().registerSchemeHandlerFactory(
                        "app",
                        "",
                        new _SchemeHandlerFactory()
                    );
                }
            });

            builder.build();
        } catch (Throwable t) {
            throw new RuntimeException("Failed to initialize JCEF.", t);
        }
    }

    @SneakyThrows
    public static CefClient createCefClient() {
        return CefApp.getInstance().createClient();
    }

    @SneakyThrows
    public static String loadResourceString(String name) {
        return StreamUtil.toString(loadResource(name), StandardCharsets.UTF_8);
    }

    @SneakyThrows
    public static InputStream loadResource(String name) {
        String fullPath = name;

        InputStream in = SaucerResourceUtil.class.getResourceAsStream(fullPath);
        if (in == null) {
            // Some IDEs mangle the resource location when launching directly. Let's try
            // that as a backup.
            in = SaucerResourceUtil.class.getResourceAsStream("/" + fullPath);
        }
        if (in == null) {
            // Another mangle.
            in = SaucerResourceUtil.class.getResourceAsStream("/resources/" + fullPath);
        }

        assert in != null : "Could not locate internal resource: " + fullPath;

        return in;
    }

}
