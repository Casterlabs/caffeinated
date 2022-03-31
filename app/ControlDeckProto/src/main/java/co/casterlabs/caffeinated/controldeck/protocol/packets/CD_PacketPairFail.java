package co.casterlabs.caffeinated.controldeck.protocol.packets;

import co.casterlabs.caffeinated.controldeck.protocol.ControlDeckPacketType;

public class CD_PacketPairFail implements ControlDeckPacket {

    @Override
    public ControlDeckPacketType getType() {
        return ControlDeckPacketType.PAIR_FAIL;
    }

}
