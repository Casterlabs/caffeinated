package co.casterlabs.caffeinated.controldeck.protocol.packets;

import java.util.Base64;

import co.casterlabs.caffeinated.controldeck.protocol.ControlDeckPacketType;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonString;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = false)
public class CD_PacketDisplayUpdate extends ControlDeckPacket {
    private byte[] pngData;

    public String getPngDataAsDataUri() {
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(this.pngData);
    }

    @JsonSerializationMethod("pngData")
    private JsonElement $serialize_pngData() {
        return new JsonString(this.getPngDataAsDataUri());
    }

    @Override
    public ControlDeckPacketType getType() {
        return ControlDeckPacketType.DISPLAY_UPDATE;
    }

}
