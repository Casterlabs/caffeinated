package co.casterlabs.caffeinated.app;

import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.kaimen.webview.WebviewWindowState;
import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonNull;
import co.casterlabs.rakurai.json.element.JsonString;

public class AppWindowState extends WebviewWindowState {

    @JsonSerializationMethod("icon")
    private JsonElement $serialize_icon() {
        try {
            return new JsonString(CaffeinatedApp.getInstance().getUI().getPreferences().getIcon());
        } catch (NullPointerException e) { // So many things can be null...
            return JsonNull.INSTANCE;
        }
    }

    @Override
    public void update() {
        super.update();

        new AsyncTask(() -> {
            CaffeinatedApp.getInstance().getWindowPreferences().save();
        });
    }

}
