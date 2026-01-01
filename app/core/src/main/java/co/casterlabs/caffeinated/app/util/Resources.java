package co.casterlabs.caffeinated.app.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import co.casterlabs.commons.io.streams.StreamUtil;

public class Resources {

    public static String string(String path) throws IOException {
        InputStream in = stream(path);
        return StreamUtil.toString(in, StandardCharsets.UTF_8);
    }

    public static byte[] bytes(String path) throws IOException {
        InputStream in = stream(path);
        return StreamUtil.toBytes(in);
    }

    public static InputStream stream(String path) throws IOException {
        InputStream in = Resources.class.getResourceAsStream(path);
        if (in == null) {
            // Some IDEs mangle the resource location when launching directly. Let's try
            // that as a backup.
            in = Resources.class.getResourceAsStream("/" + path);
        }
        if (in == null) {
            // Another mangle.
            in = Resources.class.getResourceAsStream("/resources/" + path);
        }

        assert in != null : "Could not locate internal resource: " + path;

        return in;
    }

}
