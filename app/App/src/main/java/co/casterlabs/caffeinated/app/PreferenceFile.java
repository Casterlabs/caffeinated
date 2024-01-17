package co.casterlabs.caffeinated.app;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonSerializer;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@Getter
@JsonClass(serializer = PreferenceFileSerializer.class)
public class PreferenceFile<T> {
    private FastLogger logger;
    private Class<T> clazz;
    private File file;

    private List<Consumer<PreferenceFile<T>>> saveListeners = new LinkedList<>();

    private String name;
    private @Getter(AccessLevel.NONE) T data;

    /**
     * @param clazz required to deserialize from json.
     */
    @SuppressWarnings("deprecation")
    @SneakyThrows
    public PreferenceFile(@NonNull String name, @NonNull Class<T> clazz) {
        this.name = name;
        this.clazz = clazz;
        this.logger = new FastLogger(String.format("PreferenceFile (%s)", this.name));
        this.file = new File(CaffeinatedApp.APP_DATA_DIR, String.format("preferences/%s.json", this.name));

        this.data = clazz.newInstance();

        this.load();
    }

    public T get() {
        return this.data;
    }

    public void set(@NonNull T data) {
        this.data = data;
        this.save();
    }

    public PreferenceFile<T> load() {
        try {
            if (this.file.exists()) {
                String json = new String(Files.readAllBytes(this.file.toPath()), StandardCharsets.UTF_8);
                this.data = Rson.DEFAULT.fromJson(json, this.clazz);
            } else {
                this.logger.info("Preferences file doesn't exist, creating it with defaults.");
                this.save();
            }
        } catch (StringIndexOutOfBoundsException | JsonParseException e) {
            this.logger.warn("Unable to parse preferences file, overwriting with defaults.");
            this.logger.warn(e);
            this.save();
        } catch (IOException e) {
            this.logger.severe("Unable to read preferences file: %s", e);
        }

        return this;
    }

    public PreferenceFile<T> save() {
        try {
            this.file.getParentFile().mkdirs();
            this.file.createNewFile();

            JsonElement json = Rson.DEFAULT.toJson(this.data);

            Files.write(
                this.file.toPath(),
                json
                    .toString(true)
                    .getBytes(StandardCharsets.UTF_8)
            );

            for (Consumer<PreferenceFile<T>> listener : this.saveListeners) {
                listener.accept(this);
            }

//            this.logger.log(LogLevel.TRACE, String.format("Saved preferences: %s", json));
        } catch (IOException e) {
            this.logger.severe("Unable to write preferences file: %s", e);
        }

        return this;
    }

    public PreferenceFile<T> addSaveListener(Consumer<PreferenceFile<T>> listener) {
        this.saveListeners.add(listener);
        listener.accept(this);
        return this;
    }

    public PreferenceFile<T> removeSaveListener(Consumer<PreferenceFile<T>> listener) {
        this.saveListeners.remove(listener);
        return this;
    }

    @Override
    public int hashCode() {
        return this.data == null ? 0 : this.data.hashCode();
    }

}

class PreferenceFileSerializer implements JsonSerializer<PreferenceFile<?>> {

    public PreferenceFileSerializer() {} // For Rson.

    @Override
    public JsonElement serialize(@NonNull Object value, @NonNull Rson rson) {
        return JsonSerializer.super.serialize(((PreferenceFile<?>) value).get(), rson);
    }

}
