package co.casterlabs.caffeinated.updater.animations;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.concurrent.ThreadLocalRandom;

import co.casterlabs.caffeinated.updater.window.AnimationContext;
import co.casterlabs.caffeinated.updater.window.UpdaterDialog;

public class WinterSeasonAnimation extends DialogAnimation {
    private static final int FLAKE_COUNT = 60; // MUST BE DIVISIBLE BY 3
    private static final int LAYER_FLAKE_COUNT = FLAKE_COUNT / 3;

    private static final int MIN_FLAKE_DIAMETER = 0;
    private static final int MAX_FLAKE_DIAMETER = 4;

    private static final int HORIZONTAL_SPEED = 1;

    private static final int minBoundsX = 0;
    private static final int minBoundsY = 0;
    private static final int maxBoundsX = UpdaterDialog.WIDTH;
    private static final int maxBoundsY = UpdaterDialog.HEIGHT;

    private int[][] snowflakePositions = new int[FLAKE_COUNT][4];

    public WinterSeasonAnimation() {
        // Randomize the positions
        for (int snowflakeId = 0; snowflakeId < this.snowflakePositions.length; snowflakeId++) {
            int[] snowflakeData = this.snowflakePositions[snowflakeId];

            this.resetSnowflake(snowflakeData);

            // Specially set the y.
            int y = ThreadLocalRandom.current().nextInt(minBoundsY, maxBoundsY);
            snowflakeData[1] = y;
        }

        AnimationContext
            .getRenderables()
            .add(() -> {
                for (int snowflakeId = 0; snowflakeId < this.snowflakePositions.length; snowflakeId++) {
                    int[] snowflakeData = this.snowflakePositions[snowflakeId];

                    this.moveSnowflake(snowflakeData);
                }
            });
    }

    @Override
    public void paintOnBackground(Graphics2D g2d) {
        this.paint(g2d, 0, LAYER_FLAKE_COUNT);
    }

    @Override
    public void paintOverBackground(Graphics2D g2d) {
        this.paint(g2d, LAYER_FLAKE_COUNT, LAYER_FLAKE_COUNT * 2);
    }

    @Override
    public void paintOnForeground(Graphics2D g2d) {
        this.paint(g2d, LAYER_FLAKE_COUNT * 2, LAYER_FLAKE_COUNT * 3);
    }

    // The layering system gives the animation some depth.
    // Just a cool little thing, I guess.
    private void paint(Graphics2D g2d, int layerStart, int layerEnd) {
        Color lastColor = g2d.getColor();

        g2d.setColor(Color.WHITE);

        // Loop over all snowflakes, paint their location, and move them.
        for (int snowflakeId = layerStart; snowflakeId < layerEnd; snowflakeId++) {
            int[] snowflakeData = this.snowflakePositions[snowflakeId];

            int x = snowflakeData[0];
            int y = snowflakeData[1];
            int size = snowflakeData[2];

            // Generate & fill the ellipse.
            Ellipse2D.Double circle = new Ellipse2D.Double(x, y, size, size);
            g2d.fill(circle);
        }

        // Restore the color, just in case it's needed again.
        g2d.setColor(lastColor);
    }

    private void moveSnowflake(int[] snowflakeData) {
        int verticalSpeed = snowflakeData[3];

        snowflakeData[0] += HORIZONTAL_SPEED;
        snowflakeData[1] += verticalSpeed;

        if (!this.isSnowflakeInBounds(snowflakeData)) {
            this.resetSnowflake(snowflakeData);
        }
    }

    private boolean isSnowflakeInBounds(int[] snowflakeData) {
        int x = snowflakeData[0];
        int y = snowflakeData[1];

        // We don't check the mins.
        if (x > maxBoundsX) {
            return false;
        } else if (y > maxBoundsY) {
            return false;
        }

        return true;
    }

    private void resetSnowflake(int[] snowflakeData) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        int x = random.nextInt(minBoundsX, maxBoundsX);
        int y = -2; // Above the top
        int size = random.nextInt(MIN_FLAKE_DIAMETER, MAX_FLAKE_DIAMETER + 1); // Inclusive.
        int verticalSpeed = size; // Proportional to size.

        snowflakeData[0] = x;
        snowflakeData[1] = y;
        snowflakeData[2] = size;
        snowflakeData[3] = verticalSpeed;
    }

}
