package co.casterlabs.caffeinatedapi;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;

@Getter
@JsonClass(exposeAll = true)
public class Config {
    private String spotifyClientId = "";
    private String spotifySecret = "";
    private String spotifyRedirectUri = "";

    private String streamlabsClientId = "";
    private String streamlabsSecret = "";
    private String streamlabsRedirectUri = "";

    public static Config load() throws IOException {
        return Rson.DEFAULT.fromJson(
            new String(
                Files.readAllBytes(
                    new File("plugins/CaffeinatedApi/config.json").toPath()
                ),
                StandardCharsets.UTF_8
            ),
            Config.class
        );
    }

}
