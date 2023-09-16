package co.casterlabs.caffeinated.app.fun.monitor_light;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import lombok.Getter;
import lombok.Setter;

/**
 * The ComponentResizer allows you to resize a component by dragging a border of
 * the component.
 * 
 * https://tips4java.wordpress.com/2009/09/13/resizing-components/
 */
class ComponentResizer extends MouseAdapter {
    private static final int NORTH = 1;
    private static final int WEST = 2;
    private static final int SOUTH = 4;
    private static final int EAST = 8;

    private static final Map<Integer, Integer> cursors = Map.of(
        1, Cursor.N_RESIZE_CURSOR,
        2, Cursor.W_RESIZE_CURSOR,
        4, Cursor.S_RESIZE_CURSOR,
        8, Cursor.E_RESIZE_CURSOR,
        3, Cursor.NW_RESIZE_CURSOR,
        9, Cursor.NE_RESIZE_CURSOR,
        6, Cursor.SW_RESIZE_CURSOR,
        12, Cursor.SE_RESIZE_CURSOR
    );
    private static final int dragInset = 5;
    private static final int snapSize = 1;

    private int direction;

    private Cursor sourceCursor;
    private boolean resizing;
    private Rectangle bounds;
    private Point pressed;
    private boolean autoscrolls;

    private @Getter Dimension minimumSize = new Dimension(500, 500);
    private @Getter @Setter Dimension maximumSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);

    private LightListener onChangeCallback;

    public ComponentResizer(LightListener onChangeCallback) {
        this.onChangeCallback = onChangeCallback;
    }

    public void setMinimumSize(Dimension minimumSize) {
        int minimumWidth = dragInset + dragInset;
        int minimumHeight = dragInset + dragInset;

        if (minimumSize.width < minimumWidth
            || minimumSize.height < minimumHeight) {
            String message = "Minimum size cannot be less than drag insets";
            throw new IllegalArgumentException(message);
        }

        this.minimumSize = minimumSize;
    }

    /**
     * Add the required listeners to the specified component
     *
     * @param component the component the listeners are added to
     */
    public void registerComponent(Component... components) {
        for (Component component : components) {
            component.addMouseListener(this);
            component.addMouseMotionListener(this);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Component source = e.getComponent();
        Point location = e.getPoint();
        direction = 0;

        if (location.x < dragInset)
            direction += WEST;

        if (location.x > source.getWidth() - dragInset - 1)
            direction += EAST;

        if (location.y < dragInset)
            direction += NORTH;

        if (location.y > source.getHeight() - dragInset - 1)
            direction += SOUTH;

        // Mouse is no longer over a resizable border

        if (direction == 0) {
            source.setCursor(sourceCursor);
        } else  // use the appropriate resizable cursor
        {
            int cursorType = cursors.get(direction);
            Cursor cursor = Cursor.getPredefinedCursor(cursorType);
            source.setCursor(cursor);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (!resizing) {
            Component source = e.getComponent();
            sourceCursor = source.getCursor();
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (!resizing) {
            Component source = e.getComponent();
            source.setCursor(sourceCursor);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // The mouseMoved event continually updates this variable

        if (direction == 0) return;

        // Setup for resizing. All future dragging calculations are done based
        // on the original bounds of the component and mouse pressed location.

        resizing = true;

        Component source = e.getComponent();
        pressed = e.getPoint();
        SwingUtilities.convertPointToScreen(pressed, source);
        bounds = source.getBounds();

        // Making sure autoscrolls is false will allow for smoother resizing
        // of components

        if (source instanceof JComponent) {
            JComponent jc = (JComponent) source;
            autoscrolls = jc.getAutoscrolls();
            jc.setAutoscrolls(false);
        }
    }

    /**
     * Restore the original state of the Component
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        resizing = false;

        Component source = e.getComponent();
        source.setCursor(sourceCursor);

        if (source instanceof JComponent) {
            ((JComponent) source).setAutoscrolls(autoscrolls);
        }
    }

    /**
     * Resize the component ensuring location and size is within the bounds of the
     * parent container and that the size is within the minimum and maximum
     * constraints.
     *
     * All calculations are done using the bounds of the component when the resizing
     * started.
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        if (resizing == false) return;

        Component source = e.getComponent();
        Point dragged = e.getPoint();
        SwingUtilities.convertPointToScreen(dragged, source);

        changeBounds(source, direction, bounds, pressed, dragged);
    }

    protected void changeBounds(Component source, int direction, Rectangle bounds, Point pressed, Point current) {
        // Start with original location and size
        int x = bounds.x;
        int y = bounds.y;
        int width = bounds.width;
        int height = bounds.height;

        // Resizing the West or North border affects the size and location

        if (WEST == (direction & WEST)) {
            int drag = getDragDistance(pressed.x, current.x, snapSize);
            x -= drag;
            width += drag;
        }

        if (NORTH == (direction & NORTH)) {
            int drag = getDragDistance(pressed.y, current.y, snapSize);
            y -= drag;
            height += drag;
        }

        // Resizing the East or South border only affects the size

        if (EAST == (direction & EAST)) {
            int drag = getDragDistance(current.x, pressed.x, snapSize);
            width += drag;
        }

        if (SOUTH == (direction & SOUTH)) {
            int drag = getDragDistance(current.y, pressed.y, snapSize);
            height += drag;
        }

        this.onChangeCallback.onPositionChange(x, y);
        this.onChangeCallback.onSizeChange(width, height);
        source.setBounds(x, y, width, height);
        source.validate();
    }

    /*
     *  Determine how far the mouse has moved from where dragging started
     */
    private int getDragDistance(int larger, int smaller, int snapSize) {
        int halfway = snapSize / 2;
        int drag = larger - smaller;
        drag += (drag < 0) ? -halfway : halfway;
        drag = (drag / snapSize) * snapSize;

        return drag;
    }

}
