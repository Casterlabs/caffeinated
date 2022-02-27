package co.casterlabs.caffeinated.updater.animations;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

import co.casterlabs.caffeinated.updater.util.FileUtil;
import co.casterlabs.caffeinated.updater.window.AnimationContext;
import co.casterlabs.caffeinated.updater.window.UpdaterDialog;
import lombok.SneakyThrows;

// https://www.city.semboku.akita.jp/en/sightseeing/spot/06_kamifuusen.html
// https://en.wikipedia.org/wiki/Kamif%C5%ABsen
public class KamihinokinaiAnimation extends DialogAnimation {
    private static final int LANTERN_COUNT = 9; // MUST BE DIVISIBLE BY 3
    private static final int LAYER_LANTERN_COUNT = LANTERN_COUNT / 3;

    private static final float VERTICAL_SPEED = -1;
    private static final float HORIZONTAL_SPEED = 1;

    private static final int LANTERN_SIZE = 24;

    private static final int minBoundsX = 0;
    private static final int minBoundsY = 0;
    private static final int maxBoundsX = UpdaterDialog.WIDTH;
    private static final int maxBoundsY = UpdaterDialog.HEIGHT;

    private float[][] lanternPositions = new float[LANTERN_COUNT][2];

    private BufferedImage lanternImage;

    @SneakyThrows
    public KamihinokinaiAnimation() {
        // Randomize the positions
        for (int snowflakeId = 0; snowflakeId < this.lanternPositions.length; snowflakeId++) {
            float[] snowflakeData = this.lanternPositions[snowflakeId];

            this.resetLantern(snowflakeData);

            // Specially set the y.
            int y = ThreadLocalRandom.current().nextInt(minBoundsY, maxBoundsY);
            snowflakeData[1] = y;
        }

        this.lanternImage = ImageIO.read(FileUtil.loadResourceAsUrl("animation_assets/floating_lantern.png"));

        AnimationContext
            .getRenderables()
            .add(() -> {
                for (int snowflakeId = 0; snowflakeId < this.lanternPositions.length; snowflakeId++) {
                    float[] snowflakeData = this.lanternPositions[snowflakeId];

                    this.moveLantern(snowflakeData);
                }
            });
    }

    @Override
    public void paintOnBackground(Graphics2D g2d) {
        this.paint(g2d, 0, LAYER_LANTERN_COUNT);
    }

    @Override
    public void paintOverBackground(Graphics2D g2d) {
        this.paint(g2d, LAYER_LANTERN_COUNT, LAYER_LANTERN_COUNT * 2);
    }

    @Override
    public void paintOnForeground(Graphics2D g2d) {
        this.paint(g2d, LAYER_LANTERN_COUNT * 2, LAYER_LANTERN_COUNT * 3);
    }

    private void paint(Graphics2D g2d, int layerStart, int layerEnd) {
        // Loop over all lanterns, paint their location, and move them.
        for (int snowflakeId = layerStart; snowflakeId < layerEnd; snowflakeId++) {
            float[] snowflakeData = this.lanternPositions[snowflakeId];

            int x = (int) snowflakeData[0];
            int y = (int) snowflakeData[1];
            int size = LANTERN_SIZE;

            g2d.drawImage(this.lanternImage, x, y, size, size, null);
        }
    }

    private void moveLantern(float[] snowflakeData) {
        snowflakeData[0] += HORIZONTAL_SPEED;
        snowflakeData[1] += VERTICAL_SPEED;

        if (!this.isLanternInBounds(snowflakeData)) {
            this.resetLantern(snowflakeData);
        }
    }

    private boolean isLanternInBounds(float[] snowflakeData) {
        float x = snowflakeData[0];
        float y = snowflakeData[1];

        if (x > maxBoundsX) {
            return false;
        } else if (y < minBoundsY - LANTERN_SIZE) {
            return false;
        }

        return true;
    }

    private void resetLantern(float[] snowflakeData) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        float x = random.nextInt(minBoundsX, maxBoundsX);
        float y = maxBoundsY;

        snowflakeData[0] = x;
        snowflakeData[1] = y;
    }

}
