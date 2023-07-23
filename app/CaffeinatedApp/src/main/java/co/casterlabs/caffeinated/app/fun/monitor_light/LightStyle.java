package co.casterlabs.caffeinated.app.fun.monitor_light;

import java.awt.BasicStroke;
import java.awt.Graphics2D;

public enum LightStyle {
    FULL() {
        @Override
        public void paint(Graphics2D g, int width, int height) {
            g.fillRect(0, 0, width, height);
        }
    },

    ROUND() {
        @Override
        public void paint(Graphics2D g, int width, int height) {
            final int stroke = 60;
            g.setStroke(new BasicStroke(stroke * 2));
            g.drawOval(stroke, stroke, width - stroke - stroke, height - stroke - stroke);
        }
    }

    ;

    public abstract void paint(Graphics2D g, int width, int height);

}
