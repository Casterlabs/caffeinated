package co.casterlabs.caffeinated.app.ui;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.Debouncer;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.kaimen.webview.WebviewWindowState;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class CaffeinatedWindowState extends WebviewWindowState {
    private Debouncer debouncer = new Debouncer();

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

            if (this.getX() >= monitorX && this.getX() <= (monitorX + monitorWidth) && this.getY() >= monitorY && this.getY() <= (monitorY + monitorHeight)) {
                return; // Valid position.
            }
        }

        // Invalid position, reset it.
        FastLogger.logStatic("App would've been off-screen! Resetting position to the center of the main monitor.");

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        int monitorWidth = gd.getDisplayMode().getWidth();
        int monitorHeight = gd.getDisplayMode().getHeight();

        this.setX((monitorWidth - this.getWidth()) / 2);
        this.setY((monitorHeight - this.getHeight()) / 2);
    }

    private void save_db() {
        this.debouncer.debounce(() -> {
            CaffeinatedApp.getInstance().getWindowPreferences().save();
        });
    }

    @Override
    public void setMaximized(boolean maximized) {
        super.setMaximized(maximized);
        this.save_db();
    }

    @Override
    public void setHasFocus(boolean hasFocus) {
        super.setHasFocus(hasFocus);
        this.save_db();
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        this.save_db();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        this.save_db();
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        this.save_db();
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        this.save_db();
    }

    @Override
    public int getMinWidth() {
        return 400;
    }

    @Override
    public int getMinHeight() {
        return 300;
    }

}
