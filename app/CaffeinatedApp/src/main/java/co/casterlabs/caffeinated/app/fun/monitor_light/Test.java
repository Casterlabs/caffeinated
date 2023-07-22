package co.casterlabs.caffeinated.app.fun.monitor_light;

public class Test {

    public static void main(String[] args) {
        MonitorLight light = new MonitorLight();
        light.setSize(500, 500);
        light.setOpacity(.7f);
        light.setStyle(LightStyle.ROUND);
        light.show();
    }

}
