package co.casterlabs.caffeinated.app.sdk;

import org.jetbrains.annotations.Nullable;

import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptObject;
import co.casterlabs.caffeinated.pluginsdk.Emojis;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.emoji.data.EmojiIndex;
import co.casterlabs.emoji.generator.EmojiIndexGenerator;
import lombok.Setter;

@JavascriptObject
public class EmojisImpl implements Emojis {
    public static final EmojisImpl INSTANCE = new EmojisImpl();

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
