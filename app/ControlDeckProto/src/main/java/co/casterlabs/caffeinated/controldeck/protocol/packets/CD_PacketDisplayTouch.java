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
public class CD_PacketDisplayTouch extends ControlDeckPacket {
    private int x;
    private int y;

    @Override
    public ControlDeckPacketType getType() {
        return ControlDeckPacketType.DISPLAY_TOUCH;
    }

}
