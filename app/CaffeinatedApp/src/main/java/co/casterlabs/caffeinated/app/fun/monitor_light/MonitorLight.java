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

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

public class MonitorLight {
    private static final Cursor DRAG_CURSOR = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    private static final int BORDER_WIDTH = 12;

    private final JFrame frame = new JFrame("Casterlabs Caffeinated Light");

    private final Canvas canvas = new Canvas() {
        private static final long serialVersionUID = 5366178863004795132L;

        @Override
        public void paint(Graphics g) {
            g.setColor(new Color(1, 1, 1, opacity));
            style.paint(
                (Graphics2D) g,
                frame.getWidth() - BORDER_WIDTH - BORDER_WIDTH,
                frame.getHeight() - BORDER_WIDTH - BORDER_WIDTH
            );

            g.clearRect(mouseX, mouseY, 1, 1);
        }
    };

    private Point mouseDownCompCoords = null;
    private int mouseX = -1;
    private int mouseY = -1;

    private @Getter @Setter LightStyle style = LightStyle.FULL;
    private float opacity = .75f;

    @SneakyThrows
    public MonitorLight() {
        SwingUtilities.invokeAndWait(() -> {
            this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            this.frame.setUndecorated(true);
            this.frame.setAlwaysOnTop(true);
            this.frame.setBackground(new Color(0, 0, 0, 0));

            this.frame.setLayout(new BorderLayout());
            this.frame.add(this.canvas, BorderLayout.CENTER);

            this.frame.getRootPane().setBorder(BorderFactory.createMatteBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, Color.GRAY));

            this.frame.addMouseWheelListener(new MouseWheelListener() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    float newOpacity = opacity + (float) (e.getPreciseWheelRotation() * -1 / 10);

                    if (newOpacity > 1) {
                        newOpacity = 1;
                    } else if (newOpacity < .25) {
                        newOpacity = .25f;
                    }

                    opacity = newOpacity;
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
                    mouseDownCompCoords = e.getPoint();
                }
            });
            this.frame.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
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
            this.canvas.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    mouseX = e.getX();
                    mouseY = e.getY();
                    frame.repaint();
                }
            });

            ComponentResizer cr = new ComponentResizer();
            cr.registerComponent(this.frame);
            cr.setMinimumSize(new Dimension(100, 100));
        });
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
        SwingUtilities.invokeLater(canvas::repaint);
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
    protected void finalize() throws Throwable {
        this.frame.dispose();
    }

}
