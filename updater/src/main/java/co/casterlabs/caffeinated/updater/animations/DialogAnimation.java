package co.casterlabs.caffeinated.updater.animations;

import java.awt.Graphics2D;

/**
 * All paints are animation-safe.
 */
public abstract class DialogAnimation {

    public void paintOnForeground(Graphics2D g2d) {}

    // Above the background, below the UI elements (foreground)
    public void paintOverBackground(Graphics2D g2d) {}

    public void paintOnBackground(Graphics2D g2d) {}

}
