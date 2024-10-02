package co.casterlabs.caffeinated.app.ui;

import java.awt.GraphicsEnvironment;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.io.streams.StreamUtil;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.SneakyThrows;
import okhttp3.Request;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class FontProvider {
    private static final List<String> fonts = new LinkedList<>();

    private static final List<Provider> providers = Arrays.asList(
        new SystemFontsProvider(),
        new GoogleFontsProvider()
    );

    static {
        AsyncTask.create(() -> {
            Set<String> combined = new HashSet<>();

            for (Provider provider : providers) {
                combined.addAll(provider.listFonts());
            }

            fonts.addAll(combined);
            Collections.sort(fonts);
        });
    }

    public static List<String> listFonts() {
        return Collections.unmodifiableList(fonts);
    }

}

interface Provider {

    public List<String> listFonts();

}

class GoogleFontsProvider implements Provider {
    private static final String GOOGLE_FONTS_API_KEY = "AIzaSyBuFeOYplWvsOlgbPeW8OfPUejzzzTCITM";

    @Override
    public List<String> listFonts() {
        List<String> fonts = new LinkedList<>();

        try {
            FastLogger.logStatic("Loading GoogleFonts.");

            JsonObject response = Rson.DEFAULT.fromJson(
                WebUtil.sendHttpRequest(new Request.Builder().url("https://www.googleapis.com/webfonts/v1/webfonts?sort=popularity&key=" + GOOGLE_FONTS_API_KEY)),
                JsonObject.class
            );

            if (response.containsKey("items")) {
                JsonArray items = response.getArray("items");

                for (JsonElement e : items) {
                    JsonObject font = e.getAsObject();

                    fonts.add(font.getString("family").trim());
                }
            }
        } catch (Exception e) {
            FastLogger.logException(e);
        }

        return fonts;
    }

}

class SystemFontsProvider implements Provider {
//    private static final String powershellCmd = ""
//        + "[void] [System.Reflection.Assembly]::LoadWithPartialName('System.Drawing'); "
//        + "ConvertTo-Json (New-Object System.Drawing.Text.InstalledFontCollection).Families";

    private static final String macCmd = "system_profiler SPFontsDataType | grep 'Full Name:'";

    @Override
    public List<String> listFonts() {
        try {
            switch (Platform.osDistribution) {
                case MACOS: {
                    FastLogger.logStatic("Loading macOS System fonts.");
                    return this.listMacFonts();
                }

//                case WINDOWS_NT: {
//                    FastLogger.logStatic("Loading Windows System fonts.");
//                    return this.listWindowsFonts();
//                }

                default:
                    break;
            }
        } catch (Exception e) {
            FastLogger.logException(e);
            // Fall through and use Java's listing.
        }

        try {
            FastLogger.logStatic("Loading System fonts.");

            return Arrays.asList(
                GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames()
            );
        } catch (Exception e) {
            FastLogger.logException(e);
            return Collections.emptyList();
        }
    }

    @SneakyThrows
    private List<String> listMacFonts() {
        List<String> fonts = new LinkedList<>();

        String[] list = StreamUtil.toString(
            Runtime
                .getRuntime()
                .exec(new String[] {
                        "bash",
                        "-c",
                        macCmd
                })
                .getInputStream(),
            Charset.defaultCharset()
        )
            .split("\n");

        for (String item : list) {
            String name = item.split("Full Name:")[1].trim();

            fonts.add(name);
        }

        return fonts;
    }

//    @SneakyThrows
//    private List<String> listWindowsFonts() {
//        List<String> fonts = new LinkedList<>();
//
//        String json = IOUtil.readInputStreamString(
//            Runtime
//                .getRuntime()
//                .exec(new String[] {
//                        "powershell",
//                        "-command",
//                        powershellCmd
//                })
//                .getInputStream(),
//            StandardCharsets.UTF_8
//        );
//
//        JsonArray array = Rson.DEFAULT.fromJson(json, JsonArray.class);
//        for (JsonElement e : array) {
//            String name = e
//                .getAsObject()
//                .getString("Name")
//                .trim();
//
//            fonts.add(name);
//        }
//
//        return fonts;
//    }

}
