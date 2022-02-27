package co.casterlabs.caffeinated.app.ui;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UIBackgroundColor {
    WHITE("hsl(0, 0%, 100%)"),
    BLACK("hsl(0, 0%, 4%)"),
    LIGHT("hsl(0, 0%, 96%)"),
    DARK("hsl(0, 0%, 21%)"),
    PRIMARY("hsl(171, 100%, 41%)"),
    LINK("hsl(217, 71%, 53%)"),
    INFO("hsl(204, 86%, 53%)"),
    SUCCESS("hsl(141, 71%, 48%)"),
    WARNING("hsl(48, 100%, 67%)"),
    DANGER("hsl(348, 100%, 61%)"),

    ;

    private String color;

}
