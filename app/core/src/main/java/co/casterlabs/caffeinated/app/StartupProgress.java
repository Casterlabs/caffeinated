package co.casterlabs.caffeinated.app;

import co.casterlabs.caffeinated.window.AppWindow;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class StartupProgress {
    private static final int NUM_STEPS = 12;

    private static int currentStep = 0;
    public static boolean uiInitialized = false;

    public static boolean isFinished() {
        return currentStep >= NUM_STEPS;
    }

    @SneakyThrows
    public static void increment(String name) {
        currentStep++;
        if (currentStep > NUM_STEPS) {
            FastLogger.logStatic(LogLevel.WARNING, "Startup progress exceeded the number of steps. This is likely a bug. Current step: %d, Num steps: %d, Name: %s", currentStep, NUM_STEPS, name);
        }

        double progress = Math.min(1.0, (double) currentStep / NUM_STEPS);
        FastLogger.logStatic("Startup progress: %.2f%% - %s", progress * 100, name);
        if (uiInitialized) {
            AppWindow.INSTANCE.executeJavaScript(String.format("window.__handleStartupProgress(%f, '%s');", progress, name));
        }
    }

}
