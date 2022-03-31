package co.casterlabs.caffeinated.controldeck.protocol;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.controldeck.protocol.packets.CD_PacketDeckInit;
import co.casterlabs.caffeinated.controldeck.protocol.packets.CD_PacketPCInit;
import co.casterlabs.caffeinated.controldeck.protocol.packets.CD_PacketPairFail;
import co.casterlabs.caffeinated.controldeck.protocol.packets.CD_PacketReady;
import co.casterlabs.caffeinated.controldeck.protocol.packets.CD_PacketVolume;
import co.casterlabs.caffeinated.controldeck.protocol.packets.ControlDeckPacket;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import co.casterlabs.rakurai.json.validation.JsonValidationException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public enum ControlDeckPacketType {
    INVALID(0),

    READY(1),
    DECK_INIT(2),
    PC_INIT(3),
    PAIR_FAIL(4),

    VOLUME(4),
    DISPLAY_UPDATE(5),
    DISPLAY_TOUCH(6),

    ;

    private int packetId;

    public static @Nullable ControlDeckPacket getPacket(@NonNull JsonObject json) throws JsonValidationException, JsonParseException {
        ControlDeckPacketType type = Rson.DEFAULT.fromJson(json.get("type"), ControlDeckPacketType.class);

        switch (type) {
            case DECK_INIT:
                return Rson.DEFAULT.fromJson(json, CD_PacketDeckInit.class);

            case PAIR_FAIL:
                return Rson.DEFAULT.fromJson(json, CD_PacketPairFail.class);

            case PC_INIT:
                return Rson.DEFAULT.fromJson(json, CD_PacketPCInit.class);

            case READY:
                return Rson.DEFAULT.fromJson(json, CD_PacketReady.class);

            case VOLUME:
                return Rson.DEFAULT.fromJson(json, CD_PacketVolume.class);

            case INVALID:
                break;

        }

        return null;
    }

}
