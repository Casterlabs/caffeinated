package co.casterlabs.caffeinated.updater.animations;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import co.casterlabs.caffeinated.updater.util.FileUtil;
import lombok.SneakyThrows;

public class PrideAnimation extends DialogAnimation {
    private BufferedImage prideImage;

    @SneakyThrows
    public PrideAnimation() {
        this.prideImage = ImageIO.read(FileUtil.loadResourceAsUrl("animation_assets/pride.png"));
    }

    @Override
    public void paintOnBackground(Graphics2D g2d) {
        g2d.drawImage(this.prideImage, 0, 0, null);
    }

    @Override
    public boolean shouldShowCasterlabsBanner() {
        return false;
    }

    @Override
    public String getIcon() {
        return "pride.png";
    }

}
