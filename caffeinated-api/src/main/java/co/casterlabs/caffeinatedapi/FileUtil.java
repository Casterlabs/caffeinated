package co.casterlabs.caffeinatedapi;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import co.casterlabs.rakurai.json.Rson;

public class FileUtil {

    public static <T> T read(File file, Class<T> type) throws IOException {
        return Rson.DEFAULT.fromJson(
            new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8),
            type
        );
    }

}
