package co.casterlabs.caffeinated.controldeck.protocol.packets;

import co.casterlabs.caffeinated.controldeck.protocol.ControlDeckPacketType;
import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;

public interface ControlDeckPacket {

    @JsonSerializationMethod("type")
    public ControlDeckPacketType getType();

}
