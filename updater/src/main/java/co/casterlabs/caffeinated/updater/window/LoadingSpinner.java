package co.casterlabs.caffeinated.updater.window;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import co.casterlabs.caffeinated.updater.util.FileUtil;

public class LoadingSpinner extends JPanel {
    private static final long serialVersionUID = 8420714649640311101L;

    private double rotation = 0;

    private JLabel label;

    public LoadingSpinner() throws IOException {
        this.label = new JLabel();

        // https://icons8.com/preloaders/en/circular
        // You're looking for "Full Snake"
        ImageIcon icon = new ImageIcon(FileUtil.loadResourceAsUrl("assets/loading.png"));

        this.label.setIcon(icon);

        this.add(this.label);

        this.setSize(50, 50);
        this.setPreferredSize(this.getSize());
        this.setMinimumSize(this.getSize());
        this.setMaximumSize(this.getSize());

        this.setOpaque(false);

        AnimationContext
            .getRenderables()
            .add(() -> {
                this.rotation += 5;
                this.rotation %= 360;
            });
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // I pulled these out of my ass.
        final int xOrigin = 25;
        final int yOrigin = 25;

        g2d.rotate(this.rotation / 180.0 * Math.PI, xOrigin, yOrigin);
    }

}
