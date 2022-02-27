package co.casterlabs.caffeinated.pluginsdk;

import co.casterlabs.caffeinated.pluginsdk.koi.Koi;
import co.casterlabs.caffeinated.pluginsdk.music.Music;
import lombok.Getter;

public abstract class Caffeinated {

    private static @Getter Caffeinated instance;

    public Caffeinated() {
        assert instance == null : "Caffeinated has already been initialized, you sneaky sneakerton.";

        instance = this;
    }

    public abstract Koi getKoi();

    public abstract Music getMusic();

}
