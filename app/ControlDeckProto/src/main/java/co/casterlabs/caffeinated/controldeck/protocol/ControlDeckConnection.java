package co.casterlabs.caffeinated.controldeck.protocol;

import co.casterlabs.caffeinated.controldeck.protocol.packets.ControlDeckPacket;

public interface ControlDeckConnection {

    public void sendPacket(ControlDeckPacket packet);

    public void onInit();

}
