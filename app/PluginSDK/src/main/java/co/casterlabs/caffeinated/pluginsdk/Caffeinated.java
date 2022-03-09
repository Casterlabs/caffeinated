package co.casterlabs.caffeinated.pluginsdk;

import co.casterlabs.caffeinated.pluginsdk.koi.Koi;
import co.casterlabs.caffeinated.pluginsdk.music.Music;
import lombok.SneakyThrows;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public interface Caffeinated {

    @SneakyThrows
    public static Caffeinated getInstance() {
        return ReflectionLib.invokeStaticMethod(Class.forName("co.casterlabs.caffeinated.app.CaffeinatedApp"), "getInstance");
    }

    public Koi getKoi();

    public Music getMusic();

}
