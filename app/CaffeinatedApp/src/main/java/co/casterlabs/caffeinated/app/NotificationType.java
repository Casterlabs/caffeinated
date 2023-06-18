package co.casterlabs.caffeinated.app;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum NotificationType {
    ERROR("hsl(348, 100%, 61%)"),
    WARNING("hsl(48, 100%, 67%)"),
    INFO("hsl(204, 86%, 53%)"),
    NONE("hsl(171, 100%, 41%)");

    private @Getter String color;

}
