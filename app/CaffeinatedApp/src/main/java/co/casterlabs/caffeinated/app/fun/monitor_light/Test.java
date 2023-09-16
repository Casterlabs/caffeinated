package co.casterlabs.caffeinated.app.fun.monitor_light;

public class Test {

    public static void main(String[] args) {
        MonitorLight light = new MonitorLight(new LightListener() {
            @Override
            public void onOpacityChange(double opacity) {
                System.out.printf("onOpacityChange(%f);\n", opacity);
            }

            @Override
            public void onSizeChange(int width, int height) {
                System.out.printf("onSizeChange(%d, %d);\n", width, height);
            }

            @Override
            public void onPositionChange(int x, int y) {
                System.out.printf("onPositionChange(%d, %d);\n", x, y);
            }
        });
        light.setSize(500, 500);
        light.setOpacity(.7f);
        light.setStyle(LightStyle.FULL);
        light.show();
    }

}
