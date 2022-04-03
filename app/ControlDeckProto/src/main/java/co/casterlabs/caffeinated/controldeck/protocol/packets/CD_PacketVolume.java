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
public class CD_PacketVolume extends ControlDeckPacket {
    private byte hardwareItemIndex;
    private boolean isMuted;
    private float volume;

    @Override
    public ControlDeckPacketType getType() {
        return ControlDeckPacketType.VOLUME;
    }

}
