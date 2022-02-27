package co.casterlabs.caffeinated.updater.window;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.updater.util.FileUtil;
import lombok.NonNull;

public class ImageButton extends JPanel implements MouseListener {
    private static final long serialVersionUID = 8420714649640311101L;

    private JLabel label;

    private Runnable onClickHandler;

    public ImageButton(@NonNull String asset, @Nullable Runnable onClickHandler) throws IOException {
        this.onClickHandler = onClickHandler;
        this.label = new JLabel();

        ImageIcon icon = new ImageIcon(FileUtil.loadResourceAsUrl("assets/" + asset));

        this.label.setIcon(icon);

        this.add(this.label);

        this.setSize(50, 50);
        this.setPreferredSize(this.getSize());
        this.setMinimumSize(this.getSize());
        this.setMaximumSize(this.getSize());

        this.setOpaque(false);

        if (this.onClickHandler != null) {
            this.addMouseListener(this);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        this.onClickHandler.run();
    }

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Enable antialiasing.
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        super.paintComponent(g);
    }

}
