package co.casterlabs.caffeinated.bootstrap;

import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.Collections;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.NotificationType;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.saucer.utils.SaucerApp;
import lombok.NonNull;

public class TrayHandler {
    private static CheckboxMenuItem showCheckbox;
    private static SystemTray tray;

    private static Image lastImage;
    private static TrayIcon icon;

    public static boolean tryCreateTray() {
        if (tray == null) {
            // Check the SystemTray support
            if ((Platform.osDistribution == OSDistribution.MACOS) || !SystemTray.isSupported()) {
                // Note that calling SystemTray will cause an AWT thread lock. So we always need
                // to be diligent and check for platform before compat.
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

            // TODO
//            showCheckbox.setState(Bootstrap.getSaucer().window().);

            // Add components to popup menu
            popup.add(showCheckbox);
            popup.addSeparator();
            popup.add(itemExit);

            showCheckbox.addItemListener((ItemEvent e) -> {
                // TODO
//                if (webview.isOpen()) {
//                    webview.close();
//                } else {
//                    webview.open(CaffeinatedApp.getInstance().getAppUrl());
//                }
            });

            itemExit.addActionListener((ActionEvent e) -> {
                Bootstrap.shutdown();
            });

            // Setup the tray icon.
//            icon = new TrayIcon(new ImageIcon(App.getIconURL(), "Casterlabs Logo").getImage());
//            App.on(AppEvent.ICON_CHANGE, TrayHandler::changeTrayIcon);
//
//            icon.setImageAutoSize(true);
//            icon.setPopupMenu(popup);
//
//            icon.addMouseListener(new MouseListener() {
//
//                @Override
//                public void mouseClicked(MouseEvent e) {}
//
//                @Override
//                public void mousePressed(MouseEvent e) {}
//
//                @Override
//                public void mouseReleased(MouseEvent e) {
//                    if (!e.isPopupTrigger()) {
//                        webview.open(CaffeinatedApp.getInstance().getAppUrl());
//                    }
//                }
//
//                @Override
//                public void mouseEntered(MouseEvent e) {}
//
//                @Override
//                public void mouseExited(MouseEvent e) {}
//            });
//
//            try {
//                tray.add(icon);
//            } catch (AWTException e) {}

            return true;
        } else {
            throw new IllegalStateException("Tray handler is already initialized.");
        }
    }

    public static void updateShowCheckbox(boolean newState) {
        if (showCheckbox != null) {
            showCheckbox.setState(newState);

            if (!newState && CaffeinatedApp.getInstance().canDoOneTimeEvent("caffeinated.instance.closed_to_tray")) {
                CaffeinatedApp.getInstance().notify("co.casterlabs.caffeinated.app.minimized_to_tray", Collections.emptyMap(), NotificationType.INFO);
            }
        }
    }

    private static void changeTrayIcon() {
        if (lastImage != null) {
            lastImage.flush();
        }

//        Image image = new ImageIcon(App.getIconURL()).getImage();
//        lastImage = image;
//        icon.setImage(image);
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
