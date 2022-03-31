package co.casterlabs.caffeinated.controldeck.protocol.packets;

import co.casterlabs.caffeinated.controldeck.protocol.ControlDeckModel;
import co.casterlabs.caffeinated.controldeck.protocol.ControlDeckPacketType;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonClass(exposeAll = true)
public class CD_PacketDeckInit implements ControlDeckPacket {
    private ControlDeckModel model;
    private String deckId;

    private boolean userGpioEnabled; // Unused.
    private JsonObject additionalMeta; // Unused.

    @Override
    public ControlDeckPacketType getType() {
        return ControlDeckPacketType.DECK_INIT;
    }

}
