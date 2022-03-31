package co.casterlabs.caffeinated.controldeck.protocol.deck;

import java.util.function.Consumer;

import co.casterlabs.caffeinated.controldeck.protocol.packets.CD_PacketVolume;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ControlDeckVolumeItem {
    private int id;
    private ControlDeck deck;

    private boolean muted;
    private float volume;

    private @Setter Consumer<ControlDeckVolumeItem> onUpdate;

    ControlDeckVolumeItem(int id, ControlDeck deck) {
        this.id = id;
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
                .setHardwareItem((byte) id)
                .setMuted(muted)
                .setVolume(volume)
        );

        if (this.onUpdate != null) {
            this.onUpdate.accept(this);
        }
    }

}
