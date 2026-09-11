package co.casterlabs.caffeinated.bootstrap.impl;

import java.awt.Window;

public interface NativeBootstrap {

    public void init() throws Exception;

    public void setDarkAppearance(Window window, boolean dark);

}
