package co.casterlabs.caffeinated.bootstrap;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.URL;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.NotificationType;
import co.casterlabs.caffeinated.app.ui.AppUI;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.saucer.utils.SaucerApp;
import lombok.NonNull;
import lombok.SneakyThrows;

public class TrayHandler {
    private static CheckboxMenuItem showCheckbox;
    private static SystemTray tray;

    private static Image lastImage;
    private static TrayIcon icon;

    @SneakyThrows
    public static boolean tryCreateTray() {
        if (tray != null) {
            throw new IllegalStateException("Tray handler is already initialized.");
        }

        if ((Platform.osDistribution != OSDistribution.WINDOWS_NT) || !SystemTray.isSupported()) {
            // Note that calling SystemTray will cause an AWT thread lock on macOS. So we
            // always need to be diligent and check for platform for compat.
            return false;
        }

        // Need to do this somewhere, here is good.
        SaucerApp.dispatch(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        });

        tray = SystemTray.getSystemTray();
        PopupMenu popup = new PopupMenu();

        // Create the popup menu components
        showCheckbox = new CheckboxMenuItem("Show");
        MenuItem itemExit = new MenuItem("Exit");
        MenuItem itemDevTools = new MenuItem("Open DevTools");

        showCheckbox.setState(false);

        // Add components to popup menu
        popup.add(showCheckbox);
        popup.add(itemDevTools);
        popup.addSeparator();
        popup.add(itemExit);

        showCheckbox.addItemListener((ItemEvent e) -> {
            if (Bootstrap.getSaucer().window().isVisible()) {
                Bootstrap.getSaucer().window().hide();
                CaffeinatedApp.getInstance().getUI().navigate("/blank");
                updateShowCheckbox(false);
            } else {
                CaffeinatedApp.getInstance().getUI().navigate("/");
                Bootstrap.getSaucer().window().show();
                updateShowCheckbox(true);
            }
        });

        itemDevTools.addActionListener((ActionEvent e) -> {
            Bootstrap.getSaucer().webview().setDevtoolsVisible(true);
        });

        itemExit.addActionListener((ActionEvent e) -> {
            Bootstrap.shutdown();
        });

        // Setup the tray icon.
        URL resource;
        if (CaffeinatedApp.getInstance().isDev()) {
            resource = new File("./src/main/resources/assets/logo/hardhat.png").toURI().toURL();
        } else {
            String path = "assets/logo/casterlabs.png";
            resource = AppUI.class.getClassLoader().getResource(path);
        }

        icon = new TrayIcon(ImageIO.read(resource));

        icon.setImageAutoSize(true);
        icon.setPopupMenu(popup);

        icon.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    if (!Bootstrap.getSaucer().window().isVisible()) {
                        CaffeinatedApp.getInstance().getUI().navigate("/");
                        Bootstrap.getSaucer().window().show();
                        updateShowCheckbox(true);
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });

        try {
            tray.add(icon);
        } catch (AWTException e) {}

        return true;
    }

    public static void updateShowCheckbox(boolean newState) {
        if (tray == null) return;

        if (showCheckbox != null) {
            showCheckbox.setState(newState);

            if (!newState && CaffeinatedApp.getInstance().canDoOneTimeEvent("caffeinated.instance.closed_to_tray")) {
                CaffeinatedApp.getInstance().notify("co.casterlabs.caffeinated.app.minimized_to_tray", Collections.emptyMap(), NotificationType.INFO);
            }
        }
    }

    public static void changeTrayIcon(Image newImage) {
        if (tray == null) return;

        if (lastImage != null) {
            lastImage.flush();
        }

        icon.setImage(newImage);
    }

    public static void notify(@NonNull String message, @NonNull MessageType type) {
        if (icon == null) {
            throw new IllegalStateException();
        }

        icon.displayMessage("Casterlabs Caffeinated", message, type);
    }

    public static void destroy() {
        if (tray != null) {
            SwingUtilities.invokeLater(() -> {
                tray.remove(icon);
            });
        }
    }

}
