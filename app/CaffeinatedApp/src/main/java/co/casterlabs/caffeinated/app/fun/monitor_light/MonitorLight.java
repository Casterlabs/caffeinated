package co.casterlabs.caffeinated.app.fun.monitor_light;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

public class MonitorLight {
    private final JFrame frame = new JFrame("Casterlabs Caffeinated Light");

    private final Canvas canvas = new Canvas() {
        private static final long serialVersionUID = 5366178863004795132L;

        @Override
        public void paint(Graphics g) {
            g.setColor(new Color(1, 1, 1, opacity));
            style.paint((Graphics2D) g, frame.getWidth(), frame.getHeight());
        }
    };

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

            this.frame.getRootPane().setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));

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

            new DragListener(this.frame, this.frame, this.canvas);

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
