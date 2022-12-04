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

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.kaimen.app.App;
import co.casterlabs.kaimen.app.AppEvent;
import co.casterlabs.kaimen.webview.Webview;
import lombok.NonNull;

public class TrayHandler {
    private static CheckboxMenuItem showCheckbox;
    private static SystemTray tray;

    private static Image lastImage;
    private static TrayIcon icon;

    public static boolean tryCreateTray(Webview webview) {
        if (tray == null) {
            // Check the SystemTray support
            if ((Platform.osDistribution == OSDistribution.MACOS) || !SystemTray.isSupported()) {
                // Note that calling SystemTray will cause an AWT thread lock. So we always need
                // to be diligent and check for platform before compat.
                return false;
            }

            tray = SystemTray.getSystemTray();
            PopupMenu popup = new PopupMenu();

            // Create the popup menu components
            showCheckbox = new CheckboxMenuItem("Show");
            MenuItem itemExit = new MenuItem("Exit");

            showCheckbox.setState(webview.isOpen());

            // Add components to popup menu
            popup.add(showCheckbox);
            popup.addSeparator();
            popup.add(itemExit);

            showCheckbox.addItemListener((ItemEvent e) -> {
                if (webview.isOpen()) {
                    webview.close();
                } else {
                    webview.open(CaffeinatedApp.getInstance().getAppUrl());
                }
            });

            itemExit.addActionListener((ActionEvent e) -> {
                Bootstrap.shutdown();
            });

            // Setup the tray icon.
            icon = new TrayIcon(new ImageIcon(App.getIconURL(), "Casterlabs Logo").getImage());
            App.on(AppEvent.ICON_CHANGE, TrayHandler::changeTrayIcon);

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
                        webview.open(CaffeinatedApp.getInstance().getAppUrl());
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
        } else {
            throw new IllegalStateException("Tray handler is already initialized.");
        }
    }

    public static void updateShowCheckbox(boolean newState) {
        if (showCheckbox != null) {
            showCheckbox.setState(newState);
        }
    }

    private static void changeTrayIcon() {
        if (lastImage != null) {
            lastImage.flush();
        }

        Image image = new ImageIcon(App.getIconURL()).getImage();
        lastImage = image;

        icon.setImage(image);
    }

    public static void notify(@NonNull String message, @NonNull MessageType type) {
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
