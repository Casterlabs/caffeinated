package co.casterlabs.caffeinated.updater;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.swing.UIManager;

import co.casterlabs.caffeinated.updater.animations.BlankAnimation;
import co.casterlabs.caffeinated.updater.animations.DialogAnimation;
import co.casterlabs.caffeinated.updater.animations.KamihinokinaiAnimation;
import co.casterlabs.caffeinated.updater.animations.PrideAnimation;
import co.casterlabs.caffeinated.updater.animations.ValentinesAnimation;
import co.casterlabs.caffeinated.updater.animations.WinterSeasonAnimation;
import co.casterlabs.caffeinated.updater.util.WebUtil;
import co.casterlabs.caffeinated.updater.window.UpdaterDialog;
import co.casterlabs.rakurai.io.IOUtil;
import okhttp3.Request;
import okhttp3.Response;
import xyz.e3ndr.consoleutil.ConsoleUtil;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Launcher {

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) throws Exception {
        DialogAnimation animation = new BlankAnimation();

        {
            Calendar calendar = Calendar.getInstance();

            int calendarMonth = calendar.get(Calendar.MONTH);
            int calendarDate = calendar.get(Calendar.DATE);

            // Enable the winter season animation between NOV 25 - JAN 15.
            {
                boolean isDecember = calendarMonth == Calendar.DECEMBER;
                boolean isNovemberTimeframe = (calendarMonth == Calendar.NOVEMBER) && (calendarDate >= 25);
                boolean isJanuaryTimeframe = (calendarMonth == Calendar.JANUARY) && (calendarDate <= 15);

                if (isDecember || isNovemberTimeframe || isJanuaryTimeframe) {
                    animation = new WinterSeasonAnimation();
                }
            }

            // Enable the Kamihinokinai animation on FEB 10.
            {
                boolean isFeburay = calendarMonth == Calendar.FEBRUARY;
                boolean isTheTenth = calendarDate == 10;

                if (isFeburay && isTheTenth) {
                    animation = new KamihinokinaiAnimation();
                }
            }

            // Enable the Valentine's animation on FEB 14.
            {
                boolean isFeburay = calendarMonth == Calendar.FEBRUARY;
                boolean isTheFourteenth = calendarDate == 14;

                if (isFeburay && isTheFourteenth) {
                    animation = new ValentinesAnimation();
                }
            }

            // Enable the Pride month animation during June.
            {
                boolean isJune = calendarMonth == Calendar.JUNE;

                if (isJune) {
                    animation = new PrideAnimation();
                }
            }
        }

        UpdaterDialog dialog = new UpdaterDialog(animation);

        dialog.setStatus("");
        dialog.setVisible(true);

        if (InstanceManager.trySummonInstance()) {
            FastLogger.logStatic("App already running, goodbye!");
            dialog.close();
            return;
        }

        dialog.setStatus("Checking for updates...");

        if (Updater.isLauncherOutOfDate()) {
            TimeUnit.SECONDS.sleep(1);

            switch (ConsoleUtil.getPlatform()) {
                case WINDOWS: {
                    try {
                        updateUpdaterWindows(dialog);
                        return;
                    } catch (Exception e) {
                        FastLogger.logStatic("Couldn't automagically update the updater (defaulting to the normal message):\n%s", e);
                    }
                }

                default: {
                    break;
                }
            }

            // TODO display this message better and give a button to download.
            dialog.setLoading(false);
            dialog.setStatus("Your launcher is out of date! (Download from casterlabs.co)");

            Desktop.getDesktop().browse(new URI("https://casterlabs.co"));
        } else {
            checkForUpdates(dialog);
        }
    }

    private static void updateUpdaterWindows(UpdaterDialog dialog) throws Exception {
        try (Response response = WebUtil.sendRawHttpRequest(new Request.Builder().url("https://cdn.casterlabs.co/dist/Caffeinated-Installer.exe"))) {
            final File tempInstaller = new File(System.getProperty("java.io.tmpdir"), "Caffeinated-Installer.exe");

            tempInstaller.delete();
            tempInstaller.createNewFile();

            dialog.setStatus("Downloading installer...");

            InputStream source = response.body().byteStream();
            OutputStream dest = new FileOutputStream(tempInstaller);

            double totalSize = response.body().contentLength();
            int totalRead = 0;

            byte[] buffer = new byte[IOUtil.DEFAULT_BUFFER_SIZE];
            int read = 0;

            while ((read = source.read(buffer)) != -1) {
                dest.write(buffer, 0, read);
                totalRead += read;

                double progress = totalRead / totalSize;

                dialog.setStatus(String.format("Downloading installer... (%.0f%%)", progress * 100));
                dialog.setProgress(progress);
            }

            dest.flush();

            source.close();
            dest.close();

            dialog.setProgress(-1);

            Runtime.getRuntime().exec(new String[] {
                    "powershell",
                    "-Command",
                    "\"Start-Process '" + tempInstaller.getCanonicalPath() + "' -Verb RunAs\""
            });
            TimeUnit.SECONDS.sleep(2);
            System.exit(0);
        }
    }

    private static void checkForUpdates(UpdaterDialog dialog) throws Exception {
        try {
            if (Updater.needsUpdate()) {
                Updater.downloadAndInstallUpdate(dialog);
                dialog.setStatus("Done!");
                // Artificial delay added because it'd be too jarring otherwise.
                // Heh, JARring, haha.
                TimeUnit.SECONDS.sleep(2);
            } else {
                TimeUnit.SECONDS.sleep(1);
                dialog.setStatus("You are up to date!");
            }

            Updater.launch(dialog);
        } catch (UpdaterException e) {
            dialog.setStatus(e.getMessage());

            FastLogger.logStatic(LogLevel.SEVERE, e.getMessage());
            FastLogger.logException(e.getCause());

            switch (e.getError()) {
                case LAUNCH_FAILED:
                    Updater.borkInstall();
                    TimeUnit.SECONDS.sleep(5);
                    checkForUpdates(dialog);
                    return;

                default:
                    return;
            }
        }
    }

}
