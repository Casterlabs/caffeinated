package co.casterlabs.caffeinated.pluginsdk.localization;

public interface LocaleProvider extends LocaleProcessorFunction {

    /**
     * @implSpec This is a lookup prefix. All {@code key}s will have this prefix
     *           removed from them when {@link #process(Language, String)} is
     *           called.
     * 
     * @implNote For Caffeinated widgets, your namespace will ALWAYS be a part of
     *           your prefix. So if you have a key, {@code my.example.key}, you will
     *           need to reference it as {@code com.example.my.example.key}
     *           otherwise the language system will not know where to lookup your
     *           key. Your prefix will still need to be {@code my.example.key}.
     */
    public String prefix();

    public Language language();

}
