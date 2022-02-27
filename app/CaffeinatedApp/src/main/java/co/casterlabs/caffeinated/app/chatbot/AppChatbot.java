package co.casterlabs.caffeinated.app.chatbot;

import java.lang.reflect.InvocationTargetException;

import co.casterlabs.caffeinated.app.chatbot.events.AppChatbotEventType;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import xyz.e3ndr.eventapi.EventHandler;

public class AppChatbot {

    private static EventHandler<AppChatbotEventType> handler = new EventHandler<>();

    public AppChatbot() {
        handler.register(this);
    }

    public void init() {
        // TODO :^)

    }

    public static void invokeEvent(JsonObject data, String nestedType) throws InvocationTargetException, JsonParseException {
        handler.call(
            Rson.DEFAULT.fromJson(
                data,
                AppChatbotEventType
                    .valueOf(nestedType)
                    .getEventClass()
            )
        );
    }

}
