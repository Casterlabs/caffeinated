//package co.casterlabs.caffeinated.app.ui;
//
//import co.casterlabs.caffeinated.app.CaffeinatedApp;
//import co.casterlabs.caffeinated.app.Debouncer;
//import co.casterlabs.kaimen.webview.WebviewWindowState;
//
//public class CaffeinatedWindowState extends WebviewWindowState {
//    private Debouncer debouncer = new Debouncer();
//
//    private void save_db() {
//        this.debouncer.debounce(() -> {
//            CaffeinatedApp.getInstance().getWindowPreferences().save();
//        });
//    }
//
//    @Override
//    public void setMaximized(boolean maximized) {
//        super.setMaximized(maximized);
//        this.save_db();
//    }
//
//    @Override
//    public void setHasFocus(boolean hasFocus) {
//        super.setHasFocus(hasFocus);
//        this.save_db();
//    }
//
//    @Override
//    public void setX(int x) {
//        super.setX(x);
//        this.save_db();
//    }
//
//    @Override
//    public void setY(int y) {
//        super.setY(y);
//        this.save_db();
//    }
//
//    @Override
//    public void setWidth(int width) {
//        super.setWidth(width);
//        this.save_db();
//    }
//
//    @Override
//    public void setHeight(int height) {
//        super.setHeight(height);
//        this.save_db();
//    }
//
//    @Override
//    public int getMinWidth() {
//        return 400;
//    }
//
//    @Override
//    public int getMinHeight() {
//        return 300;
//    }
//
//}
