package co.casterlabs.caffeinated.controldeck.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ControlDeckModel {
    UNKNOWN(0),

    V1_LITE(1), // 2 Faders, 4 Knobs
    V1_HEAVY(2), // TBD

    WEB(255); // 4 Faders

    private int enumOrdinal;

}
