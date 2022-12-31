package co.casterlabs.caffeinated.app;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.Emojis;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.emoji.data.EmojiIndex;
import co.casterlabs.emoji.generator.EmojiIndexGenerator;
import co.casterlabs.kaimen.webview.bridge.JavascriptFunction;
import co.casterlabs.kaimen.webview.bridge.JavascriptObject;
import lombok.Setter;

public class EmojisObj extends JavascriptObject implements Emojis {
    private static EmojiIndex emojiIndex;
    private static @Setter String emojiProvider = "system"; // Gets set by AppUI.

    static {
        AsyncTask.create(() -> {
            try {
                emojiIndex = EmojiIndexGenerator.load();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }

    @Override
    public EmojiIndex getEmojiIndex() {
        return emojiIndex;
    }

    @Override
    public String getEmojiProvider() {
        return emojiProvider;
    }

    @JavascriptFunction
    @Override
    public String matchAndReturnHTML(@Nullable String input, boolean escapeInput) {
        if (input == null || emojiIndex == null) return input;

        return emojiIndex.matchAllEmojisAndReturnHtml(input, emojiProvider, escapeInput);
    }

}
