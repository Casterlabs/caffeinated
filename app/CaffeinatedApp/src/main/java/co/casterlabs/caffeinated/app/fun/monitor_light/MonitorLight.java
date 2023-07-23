package co.casterlabs.caffeinated.app.fun.monitor_light;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.Closeable;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;

import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

public class MonitorLight implements Closeable {
    private static final Cursor DRAG_CURSOR = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    private static final int BORDER_WIDTH = 15;
    private static final Color BORDER_COLOR = new Color(200, 200, 200);

    private final JFrame frame = new JFrame("Casterlabs Caffeinated Light") {
        private static final long serialVersionUID = 913917615526213056L;

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (isMovable) {
                int width = this.getWidth();
                int height = this.getHeight();

                g.setColor(BORDER_COLOR);
                g.fillRect(0, 0, width, BORDER_WIDTH); // NORTH
                g.fillRect(0, height - BORDER_WIDTH, width, BORDER_WIDTH); // SOUTH
                g.fillRect(0, 0, BORDER_WIDTH, height); // EAST
                g.fillRect(width - BORDER_WIDTH, 0, BORDER_WIDTH, height); // WEST
            }
        };

    };

    private final Canvas canvas = new Canvas() {
        private static final long serialVersionUID = 5366178863004795132L;

        @Override
        public void paint(Graphics g) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();

            g.setColor(Color.WHITE);
            g.clearRect(0, 0, width, height);
            style.paint((Graphics2D) g, width, height);
            g.clearRect(mouseX, mouseY, 1, 1);
        }
    };

    private final NativeMouseInputListener holeListener = new NativeMouseInputListener() {
        @Override
        public void nativeMouseMoved(NativeMouseEvent e) {
            Point pt = e.getPoint();
            SwingUtilities.convertPointFromScreen(pt, canvas);

            mouseX = (int) pt.getX();
            mouseY = (int) pt.getY();
            frame.repaint();
        }

        @Override
        public void nativeMouseDragged(NativeMouseEvent e) {
            this.nativeMouseMoved(e);
        };
    };

    private final NativeKeyListener shiftListener = new NativeKeyListener() {
        @Override
        public void nativeKeyPressed(NativeKeyEvent e) {
            SwingUtilities.invokeLater(() -> {
                if (e.getKeyCode() == NativeKeyEvent.VC_SHIFT) {
                    isMovable = true;
                    frame.repaint();
                }
            });
        }

        @Override
        public void nativeKeyReleased(NativeKeyEvent e) {
            SwingUtilities.invokeLater(() -> {
                if (e.getKeyCode() == NativeKeyEvent.VC_SHIFT) {
                    isMovable = false;
                    frame.repaint();
                }
            });
        }
    };

    private int mouseX = -1;
    private int mouseY = -1;
    private boolean isMovable = false;

    private Point mouseDownCompCoords = null;

    private @Getter @Setter LightStyle style = LightStyle.FULL;

    @SneakyThrows
    public MonitorLight() {
        if (Platform.osDistribution != OSDistribution.WINDOWS_NT) {
            throw new RuntimeException("MonitorLight is only supported on windows.");
        }

        SwingUtilities.invokeAndWait(() -> {
            this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            this.frame.setUndecorated(true);
            this.frame.setAlwaysOnTop(true);
            this.frame.setBackground(new Color(0, 0, 0, 0));

            this.frame.setLayout(new BorderLayout());
            this.frame.add(this.canvas, BorderLayout.CENTER);

            this.frame.getRootPane().setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 0), BORDER_WIDTH));

            this.frame.addMouseWheelListener(new MouseWheelListener() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    if (!isMovable) return;

                    float newOpacity = frame.getOpacity() + (float) (e.getPreciseWheelRotation() * -1 / 10);

                    if (newOpacity > .9f) {
                        newOpacity = .9f;
                    } else if (newOpacity < .25f) {
                        newOpacity = .25f;
                    }

                    frame.setOpacity(newOpacity);
                    frame.repaint();
                }
            });

            this.frame.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    mouseDownCompCoords = null;
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (!isMovable) return;
                    mouseDownCompCoords = e.getPoint();
                }
            });
            this.frame.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (!isMovable) return;
                    if (mouseDownCompCoords == null) return;
                    if (frame.getCursor() != DRAG_CURSOR) return; // Stupid check to get around ComponentResizer.

                    Point currCoords = e.getLocationOnScreen();

                    int x = currCoords.x - mouseDownCompCoords.x;
                    int y = currCoords.y - mouseDownCompCoords.y;

                    frame.setLocation(x, y);
                }
            });
            this.frame.setCursor(DRAG_CURSOR);

            this.canvas.setCursor(Cursor.getDefaultCursor());

            ComponentResizer cr = new ComponentResizer();
            cr.registerComponent(this.frame);
            cr.setMinimumSize(new Dimension(100, 100));
        });

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            throw new RuntimeException(e);
        }

        GlobalScreen.addNativeMouseMotionListener(this.holeListener);
        GlobalScreen.addNativeKeyListener(this.shiftListener);
    }

    public void setOpacity(float opacity) {
        SwingUtilities.invokeLater(() -> {
            this.frame.setOpacity(opacity);
        });
    }

    @SneakyThrows
    public void setSize(int width, int height) {
        SwingUtilities.invokeAndWait(() -> {
            this.frame.setSize(width, height);
        });
    }

    @SneakyThrows
    public void show() {
        SwingUtilities.invokeAndWait(() -> {
            this.frame.setVisible(true);
            this.frame.createBufferStrategy(1);
        });
    }

    @Override
    public void close() {
        this.frame.dispose();
        GlobalScreen.removeNativeMouseMotionListener(this.holeListener);
        GlobalScreen.removeNativeKeyListener(this.shiftListener);
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException ignored) {}
    }

}
