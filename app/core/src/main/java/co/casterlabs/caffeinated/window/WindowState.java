package co.casterlabs.caffeinated.window;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import co.casterlabs.caffeinated.app.config.AppConfig;
import co.casterlabs.caffeinated.app.util.Debouncer;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonExclude;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@Getter
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = false)
public class WindowState {
    public static final int MIN_WIDTH = 400;
    public static final int MIN_HEIGHT = 300;

    private @JsonExclude Debouncer debouncer = new Debouncer();

    private int x;
    private int y;
    private int width = 800;
    private int height = 600;

    public WindowState() {
        this.resetPosition();
    }

    private void resetPosition() {
        if (Platform.osDistribution == OSDistribution.MACOS) {
            return; // Hangs on macOS
        }

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        int monitorWidth = gd.getDisplayMode().getWidth();
        int monitorHeight = gd.getDisplayMode().getHeight();

        this.x = (monitorWidth - this.width) / 2;
        this.y = (monitorHeight - this.height) / 2;
    }

    @JsonValidate
    private void $validate() {
        if (Platform.osDistribution == OSDistribution.MACOS) {
            return; // Hangs on macOS
        }

        // Check our current position is valid.
        for (GraphicsDevice monitor : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            int monitorX = monitor.getDefaultConfiguration().getBounds().x;
            int monitorY = monitor.getDefaultConfiguration().getBounds().y;
            int monitorWidth = monitor.getDisplayMode().getWidth();
            int monitorHeight = monitor.getDisplayMode().getHeight();

            if (this.x >= monitorX && this.x <= (monitorX + monitorWidth) && this.y >= monitorY && this.y <= (monitorY + monitorHeight)) {
                return; // Valid position.
            }
        }

        // Invalid position, reset it.
        FastLogger.logStatic("App would've been off-screen! Resetting position to the center of the main monitor.");
        this.resetPosition();
    }

    private void debounceSave() {
        this.debouncer.debounce(() -> {
            AppConfig.windowPreferences.save();
        });
    }

    public void setX(int x) {
        this.x = x;
        this.debounceSave();
    }

    public void setY(int y) {
        this.y = y;
        this.debounceSave();
    }

    public void setWidth(int width) {
        this.width = width;
        this.debounceSave();
    }

    public void setHeight(int height) {
        this.height = height;
        this.debounceSave();
    }

}
