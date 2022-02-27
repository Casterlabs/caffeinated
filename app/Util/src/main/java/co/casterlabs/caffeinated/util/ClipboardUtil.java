package co.casterlabs.caffeinated.util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import lombok.NonNull;

public class ClipboardUtil {

    public static void copy(@NonNull String text) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        clipboard.setContents(new StringSelection(text), null);
    }

}
