package co.casterlabs.caffeinated.controldeck.protocol.packets;

import java.util.Base64;

import co.casterlabs.caffeinated.controldeck.protocol.ControlDeckPacketType;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.element.JsonString;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonClass(exposeAll = true)
public class CD_PacketDisplayUpdate implements ControlDeckPacket {
    private byte[] pngData;

    @JsonSerializationMethod("pngData")
    private JsonString $serialize_pngData() {
        return new JsonString("data:image/png;base64," + Base64.getMimeEncoder().encodeToString(this.pngData));
    }

    @Override
    public ControlDeckPacketType getType() {
        return ControlDeckPacketType.DISPLAY_UPDATE;
    }

}
