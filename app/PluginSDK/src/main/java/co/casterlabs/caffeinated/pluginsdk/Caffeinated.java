package co.casterlabs.caffeinated.pluginsdk;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.koi.Koi;
import co.casterlabs.caffeinated.pluginsdk.music.Music;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public interface Caffeinated {

    @SneakyThrows
    public static Caffeinated getInstance() {
        return ReflectionLib.invokeStaticMethod(Class.forName("co.casterlabs.caffeinated.app.CaffeinatedApp"), "getInstance");
    }

    public Koi getKoi();

    public Music getMusic();

    public Emojis getEmojis();

    public void copyText(@NonNull String text, @Nullable String toastText);

    public void openLink(String url);

    public String getMimeForPath(String path);

    public String getLocale();

    public @Nullable CasterlabsAccount getCasterlabsAccount();

}
