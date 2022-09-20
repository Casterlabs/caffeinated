package co.casterlabs.caffeinated.pluginsdk;

import co.casterlabs.emoji.data.EmojiIndex;
import lombok.NonNull;

public interface Emojis {

    public EmojiIndex getEmojiIndex();

    public String getEmojiProvider();

    public String matchAndReturnHTML(@NonNull String input, boolean escapeInput);

}
