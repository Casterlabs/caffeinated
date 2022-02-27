package co.casterlabs.caffeinated.updater.animations;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

import co.casterlabs.caffeinated.updater.util.FileUtil;
import co.casterlabs.caffeinated.updater.window.AnimationContext;
import co.casterlabs.caffeinated.updater.window.UpdaterDialog;
import lombok.SneakyThrows;

public class ValentinesAnimation extends DialogAnimation {
    private static final int OBJECT_COUNT = 27; // MUST BE DIVISIBLE BY 3
    private static final int LAYER_OBJECT_COUNT = OBJECT_COUNT / 3;

    private static final float VERTICAL_SPEED = 5;
    private static final float HORIZONTAL_SPEED = 3;

    private static final int OBJECT_SIZE = 24;

    private static final int minBoundsX = 0;
    private static final int minBoundsY = 0;
    private static final int maxBoundsX = UpdaterDialog.WIDTH;
    private static final int maxBoundsY = UpdaterDialog.HEIGHT;

    private float[][] positions = new float[OBJECT_COUNT][3];

    private BufferedImage image;

    @SneakyThrows
    public ValentinesAnimation() {
        // Randomize the positions
        for (int objectId = 0; objectId < this.positions.length; objectId++) {
            float[] data = this.positions[objectId];

            this.resetObject(data);

            // Specially set the y.
            int y = ThreadLocalRandom.current().nextInt(minBoundsY, maxBoundsY);
            data[1] = y;
        }

        this.image = ImageIO.read(FileUtil.loadResourceAsUrl("animation_assets/heart.png"));

        AnimationContext
            .getRenderables()
            .add(() -> {
                for (int objectId = 0; objectId < this.positions.length; objectId++) {
                    float[] data = this.positions[objectId];

                    this.moveObject(data);
                }
            });
    }

    @Override
    public void paintOnBackground(Graphics2D g2d) {
        this.paint(g2d, 0, LAYER_OBJECT_COUNT);
    }

    @Override
    public void paintOverBackground(Graphics2D g2d) {
        this.paint(g2d, LAYER_OBJECT_COUNT, LAYER_OBJECT_COUNT * 2);
    }

    @Override
    public void paintOnForeground(Graphics2D g2d) {
        this.paint(g2d, LAYER_OBJECT_COUNT * 2, LAYER_OBJECT_COUNT * 3);
    }

    private void paint(Graphics2D g2d, int layerStart, int layerEnd) {
        // Loop over all lanterns, paint their location, and move them.
        for (int objectId = layerStart; objectId < layerEnd; objectId++) {
            float[] data = this.positions[objectId];

            int x = (int) data[0];
            int y = (int) data[1];
            int size = OBJECT_SIZE;

            g2d.drawImage(this.image, x, y, size, size, null);
        }
    }

    private void moveObject(float[] data) {
        data[0] += data[2];
        data[1] += VERTICAL_SPEED;

        if (!this.isObjectInBounds(data)) {
            this.resetObject(data);
        }
    }

    private boolean isObjectInBounds(float[] data) {
        float x = data[0];
        float y = data[1];

        if (x > maxBoundsX) {
            return false;
        } else if (y > maxBoundsY) {
            return false;
        }

        return true;
    }

    private void resetObject(float[] data) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        float x = random.nextInt(minBoundsX, maxBoundsX);
        float y = minBoundsY - OBJECT_SIZE;
        float speed = (float) (HORIZONTAL_SPEED / random.nextDouble(1, 2));

        data[0] = x;
        data[1] = y;
        data[2] = speed;
    }

}
