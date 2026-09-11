package co.casterlabs.caffeinated.bootstrap.impl.windows.common;

import java.awt.Window;

import com.sun.jna.platform.win32.WinDef.BOOL;
import com.sun.jna.platform.win32.WinDef.BOOLByReference;
import com.sun.jna.platform.win32.WinDef.HWND;

import co.casterlabs.caffeinated.app.music_integration.MusicImpl;
import co.casterlabs.caffeinated.bootstrap.impl.NativeBootstrap;
import co.casterlabs.caffeinated.bootstrap.impl.windows.common.music.WindowsSystemPlaybackMusicProvider;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public class WindowsBootstrap implements NativeBootstrap {
    private static final BOOLByReference TRUE_PTR = new BOOLByReference(new BOOL(true));
    private static final BOOLByReference FALSE_PTR = new BOOLByReference(new BOOL(false));

    @Override
    public void init() throws Exception {
        ReflectionLib.setStaticValue(MusicImpl.class, "systemPlaybackMusicProvider", new WindowsSystemPlaybackMusicProvider());
    }

    @Override
    public void setDarkAppearance(Window window, boolean dark) {
        if (!window.isDisplayable()) {
            return;
        }

        // References:
        // https://docs.microsoft.com/en-us/windows/win32/api/dwmapi/nf-dwmapi-dwmsetwindowattribute
        // https://winscp.net/forum/viewtopic.php?t=30088
        // https://gist.github.com/rossy/ebd83ba8f22339ce25ef68bfc007dfd2
        //
        // This is the code that we're mimicking (in c):
        /*
        DwmSetWindowAttribute(
            hwnd, 
            DWMWA_USE_IMMERSIVE_DARK_MODE,
            &(BOOL) { TRUE }, 
            sizeof(BOOL)
        );
        */

        HWND hwnd = DWM.getHWND(window);
        BOOLByReference attribute = dark ? TRUE_PTR : FALSE_PTR;

        DWM.INSTANCE.DwmSetWindowAttribute(
            hwnd,
            DWM.DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1,
            attribute,
            BOOL.SIZE
        );

        DWM.INSTANCE.DwmSetWindowAttribute(
            hwnd,
            DWM.DWMWA_USE_IMMERSIVE_DARK_MODE,
            attribute,
            BOOL.SIZE
        );

        FastLogger.logStatic(
            LogLevel.DEBUG,
            "Set IMMERSIVE_DARK_MODE and USE_IMMERSIVE_DARK_MODE_BEFORE_20H1 to %b.",
            dark
        );
    }

}
