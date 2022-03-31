package co.casterlabs.caffeinated.controldeck.protocol.packets;

import co.casterlabs.caffeinated.controldeck.protocol.ControlDeckPacketType;

public class CD_PacketReady implements ControlDeckPacket {

    @Override
    public ControlDeckPacketType getType() {
        return ControlDeckPacketType.READY;
    }

}
