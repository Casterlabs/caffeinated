package co.casterlabs.caffeinated.controldeck.protocol.deck;

import java.io.Closeable;
import java.util.Objects;

import co.casterlabs.caffeinated.controldeck.protocol.ControlDeckConnection;
import co.casterlabs.caffeinated.controldeck.protocol.packets.CD_PacketDeckInit;
import co.casterlabs.caffeinated.controldeck.protocol.packets.CD_PacketDisplayTouch;
import co.casterlabs.caffeinated.controldeck.protocol.packets.CD_PacketPCInit;
import co.casterlabs.caffeinated.controldeck.protocol.packets.CD_PacketReady;
import co.casterlabs.caffeinated.controldeck.protocol.packets.CD_PacketVolume;
import co.casterlabs.caffeinated.controldeck.protocol.packets.ControlDeckPacket;
import co.casterlabs.rakurai.json.Rson;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@Getter
public class ControlDeck implements Closeable {
    @Getter(AccessLevel.NONE)
    ControlDeckConnection conn;
    private @Getter(AccessLevel.NONE) int initState = 0;

    private int volumeItemsCount;
    private @Accessors(fluent = true) boolean hasDisplay;
    private String deckId;

    private ControlDeckDisplay display;
    private ControlDeckVolumeItem[] volumeItems;

    public ControlDeck(@NonNull ControlDeckConnection conn) {
        this.conn = conn;
    }

    public ControlDeckVolumeItem[] getVolumeItems() {
        return this.volumeItems.clone(); // Prevent back modification.
    }

    public void init() {
        if (this.initState == 0) {
            this.conn.sendPacket(new CD_PacketReady());
            this.initState = 1;
        }
    }

    public void processPacket(ControlDeckPacket packet) {
        FastLogger.logStatic("\u2193 %s", Rson.DEFAULT.toJson(packet));

        if (this.initState == 1) {
            CD_PacketDeckInit deckInit = (CD_PacketDeckInit) packet;

            this.volumeItemsCount = deckInit.getVolumeItemsCount();
            this.hasDisplay = deckInit.hasDisplay();
            this.deckId = deckInit.getDeckId();

            this.volumeItems = new ControlDeckVolumeItem[this.volumeItemsCount];
            for (int idx = 0; idx < this.volumeItems.length; idx++) {
                this.volumeItems[idx] = new ControlDeckVolumeItem(idx, this);
            }

            if (this.hasDisplay) {
                this.display = new ControlDeckDisplay(deckInit.getDisplayWidth(), deckInit.getDisplayHeight(), this);
            }

            this.conn.sendPacket(
                new CD_PacketPCInit()
                    .setAccepted(true)
            );

            this.initState = 2;
        } else if (this.initState == 2) {
            if (packet instanceof CD_PacketReady) {
                // READY.
                this.initState = -1;

                conn.onInit();

                for (int idx = 0; idx < this.volumeItems.length; idx++) {
                    ControlDeckVolumeItem item = this.volumeItems[idx];

                    conn.sendPacket(
                        new CD_PacketVolume()
                            .setHardwareItemIndex((byte) idx)
                            .setMuted(item.isMuted())
                            .setVolume(item.getVolume())
                    );
                }

                this.conn.sendPacket(new CD_PacketReady());
            } else {
                // Connection failed. (CD_PairFail)
                this.initState = -2;
            }
        } else {
            switch (packet.getType()) {
                case VOLUME:
                    CD_PacketVolume volumePacket = (CD_PacketVolume) packet;

                    this.volumeItems[volumePacket.getHardwareItemIndex()].update(volumePacket);
                    break;

                case DISPLAY_TOUCH:
                    this.display.onPacketDisplayTouch((CD_PacketDisplayTouch) packet);
                    break;

                default:
                    break;
            }
        }
    }

    public boolean isReady() {
        return this.initState == -1;
    }

    @Override
    public void close() {
        this.display.close();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.hasDisplay, this.deckId, this.volumeItems);
    }

}
