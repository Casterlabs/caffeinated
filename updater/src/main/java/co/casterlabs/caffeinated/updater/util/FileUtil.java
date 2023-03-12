package co.casterlabs.caffeinated.updater.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import co.casterlabs.rakurai.io.IOUtil;

public class FileUtil {

    public static String readFile(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    public static String loadResource(String path) throws IOException {
        InputStream in = FileUtil.class.getClassLoader().getResourceAsStream(path);

        return IOUtil.readInputStreamString(in, StandardCharsets.UTF_8);
    }

    public static byte[] loadResourceBytes(String path) throws IOException {
        InputStream in = FileUtil.class.getClassLoader().getResourceAsStream(path);

        return IOUtil.readInputStreamBytes(in);
    }

    public static URL loadResourceAsUrl(String path) throws IOException {
        return FileUtil.class.getClassLoader().getResource(path);
    }

    public static boolean isDirectoryEmpty(File dir) {
        return dir.listFiles().length == 0;
    }

    public static void emptyDirectory(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                emptyDirectory(file);
            }

            file.delete();
        }
    }

}
