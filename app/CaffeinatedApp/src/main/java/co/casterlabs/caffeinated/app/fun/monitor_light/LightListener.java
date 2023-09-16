package co.casterlabs.caffeinated.app.fun.monitor_light;

public interface LightListener {

    public void onOpacityChange(double opacity);

    public void onSizeChange(int width, int height);

    public void onPositionChange(int x, int y);

}
