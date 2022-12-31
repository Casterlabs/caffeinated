package co.casterlabs.caffeinated.pluginsdk;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.emoji.data.EmojiIndex;

public interface Emojis {

    public EmojiIndex getEmojiIndex();

    public String getEmojiProvider();

    public String matchAndReturnHTML(@Nullable String input, boolean escapeInput);

}
