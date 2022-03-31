package co.casterlabs.caffeinated.controldeck.protocol.packets;

import co.casterlabs.caffeinated.controldeck.protocol.ControlDeckPacketType;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonClass(exposeAll = true)
public class CD_PacketPCInit implements ControlDeckPacket {
    private boolean accepted;
    private int pairingId;

    @Override
    public ControlDeckPacketType getType() {
        return ControlDeckPacketType.PC_INIT;
    }

}
