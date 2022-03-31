package co.casterlabs.caffeinated.controldeck.protocol.packets;

import co.casterlabs.caffeinated.controldeck.protocol.ControlDeckPacketType;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonClass(exposeAll = true)
public class CD_PacketDisplayTouch implements ControlDeckPacket {
    private int x;
    private int y;

    @Override
    public ControlDeckPacketType getType() {
        return ControlDeckPacketType.DISPLAY_TOUCH;
    }

}
