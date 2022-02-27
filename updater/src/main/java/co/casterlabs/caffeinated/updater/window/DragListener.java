package co.casterlabs.caffeinated.updater.window;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DragListener extends MouseAdapter {
    private final JDialog dialog;

    private Point mouseDownCompCoords = null;

    @Override
    public void mouseReleased(MouseEvent e) {
        this.mouseDownCompCoords = null;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.mouseDownCompCoords = e.getPoint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (this.mouseDownCompCoords != null) {
            Point currCoords = e.getLocationOnScreen();

            int x = currCoords.x - this.mouseDownCompCoords.x;
            int y = currCoords.y - this.mouseDownCompCoords.y;

            this.dialog.setLocation(x, y);
        }
    }

}
