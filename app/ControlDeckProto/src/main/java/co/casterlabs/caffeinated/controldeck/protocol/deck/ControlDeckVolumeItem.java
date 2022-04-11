package co.casterlabs.caffeinated.controldeck.protocol.deck;

import java.util.Objects;
import java.util.function.Consumer;

import co.casterlabs.caffeinated.controldeck.protocol.packets.CD_PacketVolume;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ControlDeckVolumeItem {
    private ControlDeck deck;

    @JsonField
    private int index;

    @JsonField
    private boolean muted;

    @JsonField
    private float volume;

    private @Setter Consumer<ControlDeckVolumeItem> onUpdate;

    ControlDeckVolumeItem(int index, ControlDeck deck) {
        this.index = index;
        this.deck = deck;
    }

    protected void update(CD_PacketVolume volumePacket) {
        this.muted = volumePacket.isMuted();
        this.volume = volumePacket.getVolume();

        if (this.onUpdate != null) {
            this.onUpdate.accept(this);
        }
    }

    public void update(boolean muted, float volume) {
        this.muted = muted;
        this.volume = volume;

        this.deck.conn.sendPacket(
            new CD_PacketVolume()
                .setHardwareItemIndex((byte) index)
                .setMuted(muted)
                .setVolume(volume)
        );

        if (this.onUpdate != null) {
            this.onUpdate.accept(this);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.index, this.deck, this.muted, this.volume);
    }

}
