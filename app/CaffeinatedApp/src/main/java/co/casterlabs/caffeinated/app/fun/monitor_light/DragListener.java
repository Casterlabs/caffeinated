package co.casterlabs.caffeinated.app.fun.monitor_light;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;

class DragListener extends MouseAdapter {
    private static final Cursor DRAG_CURSOR = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);

    private final JFrame frame;
    private Point mouseDownCompCoords = null;

    public DragListener(JFrame frame, Component... components) {
        this.frame = frame;
        this.frame.setCursor(DRAG_CURSOR);

        for (Component c : components) {
            c.addMouseListener(this);
            c.addMouseMotionListener(this);
        }
    }

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
        if (this.mouseDownCompCoords == null) return;
        if (this.frame.getCursor() != DRAG_CURSOR) return; // Stupid check to get around ComponentResizer.

        Point currCoords = e.getLocationOnScreen();

        int x = currCoords.x - this.mouseDownCompCoords.x;
        int y = currCoords.y - this.mouseDownCompCoords.y;

        this.frame.setLocation(x, y);
    }

}
