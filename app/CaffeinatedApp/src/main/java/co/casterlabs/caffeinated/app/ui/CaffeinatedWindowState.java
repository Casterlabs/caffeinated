package co.casterlabs.caffeinated.app.ui;

import co.casterlabs.kaimen.webview.WebviewWindowState;

public class CaffeinatedWindowState extends WebviewWindowState {

    @Override
    public int getMinWidth() {
        return 400;
    }

    @Override
    public int getMinHeight() {
        return 300;
    }

}
