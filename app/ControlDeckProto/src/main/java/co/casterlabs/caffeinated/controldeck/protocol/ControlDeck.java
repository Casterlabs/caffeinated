package co.casterlabs.caffeinated.controldeck.protocol;

import java.util.function.Consumer;

import co.casterlabs.caffeinated.controldeck.protocol.packets.CD_PacketDeckInit;
import co.casterlabs.caffeinated.controldeck.protocol.packets.CD_PacketPCInit;
import co.casterlabs.caffeinated.controldeck.protocol.packets.CD_PacketReady;
import co.casterlabs.caffeinated.controldeck.protocol.packets.CD_PacketVolume;
import co.casterlabs.caffeinated.controldeck.protocol.packets.ControlDeckPacket;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
public class ControlDeck {
    private @Getter(AccessLevel.NONE) Consumer<ControlDeckPacket> connection;
    private @Getter(AccessLevel.NONE) Runnable onReady;
    private @Getter(AccessLevel.NONE) int initState = 0;

    private ControlDeckModel model;
    private String deckId;
    private VolumeItem[] volumeStates;

    public ControlDeck(@NonNull Consumer<ControlDeckPacket> connection, @NonNull Runnable onReady) {
        this.connection = connection;
        this.onReady = onReady;
    }

    public VolumeItem[] getVolumeStates() {
        return this.volumeStates.clone(); // Prevent back modification.
    }

    public void init() {
        if (this.initState == 0) {
            this.connection.accept(new CD_PacketReady());
            this.initState = 1;
        }
    }

    public void processPacket(ControlDeckPacket packet) {
        if (this.initState == 1) {
            CD_PacketDeckInit deckInit = (CD_PacketDeckInit) packet;

            this.model = deckInit.getModel();
            this.deckId = deckInit.getDeckId();

            switch (this.model) {

                case WEB:
                    this.volumeStates = new VolumeItem[4];
                    break;

                default:
                    this.volumeStates = new VolumeItem[0];
                    break;
            }

            for (int idx = 0; idx < this.volumeStates.length; idx++) {
                final int id = idx;

                this.volumeStates[idx] = new VolumeItem() {
                    @Override
                    protected void update0(boolean muted, float volume) {
                        connection.accept(
                            new CD_PacketVolume()
                                .setHardwareItem((byte) id)
                                .setMuted(muted)
                                .setVolume(volume)
                        );
                    }
                };

                this.volumeStates[idx].id = id;
            }

            this.connection.accept(
                new CD_PacketPCInit()
                    .setAccepted(true)
            );

            this.initState = 2;
        } else if (this.initState == 2) {
            // READY.
            this.initState = -1;
            this.onReady.run();

            for (int idx = 0; idx < this.volumeStates.length; idx++) {
                VolumeItem item = this.volumeStates[idx];

                connection.accept(
                    new CD_PacketVolume()
                        .setHardwareItem((byte) idx)
                        .setMuted(item.muted)
                        .setVolume(item.volume)
                );
            }

            this.connection.accept(new CD_PacketReady());
        } else {
            if (packet instanceof CD_PacketVolume) {
                CD_PacketVolume volumePacket = (CD_PacketVolume) packet;

                this.volumeStates[volumePacket.getHardwareItem()].update(volumePacket);
            }
        }
    }

    public boolean isReady() {
        return this.initState == -1;
    }

    @Getter
    public static abstract class VolumeItem {
        private int id;
        private boolean muted;
        private float volume;

        private @Setter Consumer<VolumeItem> onUpdate;

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

            this.update0(muted, volume);

            if (this.onUpdate != null) {
                this.onUpdate.accept(this);
            }
        }

        protected abstract void update0(boolean muted, float volume);

    }

}
