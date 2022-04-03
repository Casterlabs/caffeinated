package co.casterlabs.caffeinated.controldeck.protocol.packets;

import co.casterlabs.caffeinated.controldeck.protocol.ControlDeckPacketType;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = false)
public class CD_PacketPCInit extends ControlDeckPacket {
    private boolean accepted;
    private int pairingId;

    @Override
    public ControlDeckPacketType getType() {
        return ControlDeckPacketType.PC_INIT;
    }

}
