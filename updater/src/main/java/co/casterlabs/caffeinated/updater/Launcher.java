package co.casterlabs.caffeinated.updater;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.swing.UIManager;

import co.casterlabs.caffeinated.updater.animations.KamihinokinaiAnimation;
import co.casterlabs.caffeinated.updater.animations.ValentinesAnimation;
import co.casterlabs.caffeinated.updater.animations.WinterSeasonAnimation;
import co.casterlabs.caffeinated.updater.window.UpdaterDialog;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Launcher {

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) throws Exception {
        UpdaterDialog dialog = new UpdaterDialog();

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
                    dialog.getPane().setCurrentAnimation(new WinterSeasonAnimation());
                }
            }

            // Enable the Kamihinokinai animation on FEB 10.
            {
                boolean isFeburay = calendarMonth == Calendar.FEBRUARY;
                boolean isTheTenth = calendarDate == 10;

                if (isFeburay && isTheTenth) {
                    dialog.getPane().setCurrentAnimation(new KamihinokinaiAnimation());
                }
            }

            // Enable the Valentine's animation on FEB 14.
            {
                boolean isFeburay = calendarMonth == Calendar.FEBRUARY;
                boolean isTheFourteenth = calendarDate == 14;

                if (isFeburay && isTheFourteenth) {
                    dialog.getPane().setCurrentAnimation(new ValentinesAnimation());
                }
            }
        }

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
            // TODO display this message better and give a button to download.
            dialog.setLoading(false);
            dialog.setStatus("Your launcher is out of date! (Download from casterlabs.co)");
        } else {
            checkForUpdates(dialog);
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
