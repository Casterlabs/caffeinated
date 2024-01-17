package co.casterlabs.caffeinated.app.music_integration.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.pluginsdk.music.MusicPlaybackState;
import co.casterlabs.caffeinated.pluginsdk.music.MusicProvider;
import co.casterlabs.caffeinated.pluginsdk.music.MusicTrack;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonElement;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
@JsonClass(exposeAll = true)
public abstract class InternalMusicProvider<T> implements MusicProvider {
    private final String serviceName;
    private final String serviceId;
    private final Class<T> settingsClass;

    private boolean isSignedIn;
    private String accountName;
    private String accountLink;

    private MusicPlaybackState playbackState = MusicPlaybackState.INACTIVE;
    private MusicTrack currentTrack = null;

    private T settings;

    public abstract void init();

    @SuppressWarnings("unchecked")
    protected void updateSettings(@NonNull Object settings) {
        this.settings = (T) settings;
        this.onSettingsUpdate();
        CaffeinatedApp.getInstance().getMusic().save(this); // Auto updates bridge data.
    }

    @SuppressWarnings("deprecation")
    public void updateSettingsFromJson(@Nullable JsonElement settings) {
        try {
            if (settings == null) {
                this.updateSettings(this.settingsClass.newInstance());
            } else {
                this.updateSettings(Rson.DEFAULT.fromJson(settings, this.settingsClass));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void onSettingsUpdate();

    protected void setAccountData(boolean isSignedIn, String accountName, String accountLink) {
        if (isSignedIn) {
            this.isSignedIn = true;
            this.accountName = accountName;
            this.accountLink = accountLink;
            CaffeinatedApp.getInstance().getMusic().updateBridgeData();
        } else {
            this.isSignedIn = false;
            this.accountName = null;
            this.accountLink = null;
            this.setPlaybackStateInactive(); // Will autoupdate bridge.
        }
    }

    protected void setPlaybackStateInactive() {
        this.playbackState = MusicPlaybackState.INACTIVE;
        this.currentTrack = null;
        CaffeinatedApp.getInstance().getMusic().updateBridgeData();
    }

    protected void setPlaying(@NonNull MusicTrack track) {
        this.playbackState = MusicPlaybackState.PLAYING;
        this.currentTrack = track;
        CaffeinatedApp.getInstance().getMusic().updateBridgeData();
    }

    protected void setPaused(@NonNull MusicTrack track) {
        this.playbackState = MusicPlaybackState.PAUSED;
        this.currentTrack = track;
        CaffeinatedApp.getInstance().getMusic().updateBridgeData();
    }

    protected void makePaused() {
        if (this.currentTrack != null) {
            this.playbackState = MusicPlaybackState.PAUSED;
            CaffeinatedApp.getInstance().getMusic().updateBridgeData();
        }
    }

    public abstract void signout();

    public static Pair<String, List<String>> parseTitleForArtists(
        @NonNull String title,
        @NonNull List<String> prediscoveredArtists
    ) {
        final boolean PARSE_FT = true;
        final boolean CLEANSE_TITLE = true;
        final String FT_REGEX = "(\\(ft.*\\))|(\\(feat.*\\))|(\\(avec.*\\))";
        final String CT_REGEX = "(- [0-9]* Remaster)|(- Remastered [0-9]*)|(- Radio Version)|(- Radio Edit)|(\\(Remastered\\))";

        List<String> artists = new ArrayList<>();

        artists.addAll(prediscoveredArtists);

        if (PARSE_FT) {
            Matcher m = Pattern.compile(FT_REGEX).matcher(title);

            if (m.find()) {
                title = title.replaceFirst(FT_REGEX, ""); // Remove (ft. ...) from the title

                String group = m.group().split(" ", 2)[1]; // Remove the (ft.
                group = group.substring(0, group.length() - 1); // Remove the ending brace.

                String[] found = group.split("(et)|[,&]");

                for (String artist : found) {
                    artist = artist.trim();

                    // Prevent duplicates.
                    if (!artists.contains(artist)) {
                        artists.add(artist);
                    }
                }
            }
        }

        if (CLEANSE_TITLE) {
            title = title.replaceAll(CT_REGEX, "");
        }

        return new Pair<>(title, artists);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.serviceId, this.currentTrack, this.isSignedIn);
    }

}
