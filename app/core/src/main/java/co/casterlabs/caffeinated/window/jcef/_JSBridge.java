package co.casterlabs.caffeinated.window.jcef;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;

import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptObject;
import co.casterlabs.caffeinated.window.AppWindow;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.element.JsonString;
import lombok.NonNull;
import lombok.SneakyThrows;

class _JSBridge extends CefMessageRouterHandlerAdapter {
    private static final String init_fmt = _CefUtil.loadResourceString("jcef/bridge/init_fmt.js");
    private static final String ipc_object_fmt = _CefUtil.loadResourceString("jcef/bridge/ipc_object_fmt.js");

    private final Map<String, _JavascriptObjectWrapper> objects = new LinkedHashMap<>();

    public final _JSMessages messages = new _JSMessages();
    private final List<String> initScripts = new LinkedList<>();

    public _JSBridge() {
        this.initScripts.add(
            String.format(
                init_fmt,
                new JsonObject()
//                    .put("archTarget", SaucerApp.archTarget())
//                    .put("systemTarget", SaucerApp.systemTarget())
//                    .put("backend", SaucerApp.backendType().toString())
            )
        );

//        this.defineObject("saucer.webview", this.webview);
//        this.defineObject("saucer.window", this.webview.window);
//        this.defineObject("saucer.app", SaucerApp.class);
    }

    public void onLoadStart(CefFrame frame) {
        System.out.println("Injecting bridge scripts into webview.");
        for (String script : this.initScripts) {
            frame.executeJavaScript(script, "bridge", 0);
        }
    }

    @Override
    public boolean onQuery(CefBrowser browser, CefFrame frame, long queryId, String _request, boolean persistent, CefQueryCallback callback) {
        JsonObject message;
        try {
            // CEF has a weird internal way of handling strings, and unfortunately
            // the JNI wrapper mangles them. We've opted to just use URI encoding
            // to prevent manglage. decodeURIComponent implementation taken from here:
            // https://stackoverflow.com/a/6926987/11611152
            String raw = URLDecoder.decode(_request.replace("+", "%2B"), "UTF-8")
                .replace("%2B", "+");

            message = Rson.DEFAULT.fromJson(raw, JsonObject.class).getObject("message");
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }

        JsonElement returnValue = null;
        boolean isError = false;
        try {
            switch (message.getString("type")) {
                case "GET": {
                    _JavascriptObjectWrapper object = this.objects.get(message.getString("objectId"));
                    assert object != null : "Unknown objectId: " + message;

                    // RPC.get("objectId", "propertyName");
                    returnValue = object.handleGet(message.getString("propertyName"));
                    break;
                }

                case "SET": {
                    _JavascriptObjectWrapper object = this.objects.get(message.getString("objectId"));
                    assert object != null : "Unknown objectId: " + message;

                    // RPC.set("objectId", "propertyName", newValue);
                    object.handleSet(message.getString("propertyName"), message.get("newValue"));
                    break;
                }

                case "INVOKE": {
                    _JavascriptObjectWrapper object = this.objects.get(message.getString("objectId"));
                    assert object != null : "Unknown objectId: " + message;

                    // RPC.invoke("objectId", "functionName", Array.from(arguments));
                    returnValue = object.handleInvoke(message.getString("functionName"), message.getArray("arguments"));
                    break;
                }

                case "MESSAGE": {
                    JsonElement data = message.get("data");
                    this.messages.handle(data);
                    break;
                }

                case "CHECK_MUTATION": {
                    JsonObject newValues = new JsonObject();
                    for (_JavascriptObjectWrapper object : this.objects.values()) {
                        for (String name : object.whichFieldsHaveMutated()) {
                            newValues.put(object.id + '|' + name, object.handleGet(name));
                        }
                    }
                    returnValue = newValues;
                    break;
                }

                case "OPEN_LINK": {
                    String link = message.getString("link");
                    AppWindow.INSTANCE.open(link);
                    break;
                }

                default:
                    throw new IllegalArgumentException("Unrecognized call: " + message);
            }
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            t.printStackTrace(pw);

            String out = sw.toString();

            pw.flush();
            pw.close();
            sw.flush();

            String full = out
                .substring(0, out.length() - 2)
                .replace("\r", "");

            System.err.printf("An error occurred whilst processing function, bubbling to JavaScript.\n%s\n", full);
            returnValue = new JsonString(full);
            isError = true;
        }

        JsonElement requestId = message.get("requestId");
        if (requestId == null) {
            // Drop the response.
        } else if (isError) {
            String js = String.format(
                "if (window.saucer.__rpc.waiting[%s]) window.saucer.__rpc.waiting[%s].reject(%s);",
                requestId, requestId, returnValue
            );
            AppWindow.INSTANCE.executeJavaScript(js);
        } else {
            String js = String.format(
                "if (window.saucer.__rpc.waiting[%s]) window.saucer.__rpc.waiting[%s].resolve(%s);",
                requestId, requestId, returnValue
            );
            AppWindow.INSTANCE.executeJavaScript(js);
        }

        return true;
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    /**
     * Executes the given JavaScript code in the webview.
     * 
     * @return this instance, for chaining.
     */
    @JavascriptFunction
    public _JSBridge executeJavaScript(@NonNull String scriptToExecute) {
        AppWindow.INSTANCE.executeJavaScript(scriptToExecute);
        return this;
    }

    /**
     * @param  obj your class annotated with {@link JavascriptObject}. Can also be
     *             an instance of a class (i.e an object).
     * 
     * @return     this instance, for chaining.
     */
    public _JSBridge defineObject(@NonNull String name, @NonNull Object obj) {
        this.internal_defineObject(name, obj);
        return this;
    }

    @SneakyThrows
    private _JavascriptObjectWrapper internal_defineObject(String name, Object obj) {
        Class<?> clazz;
        if (obj instanceof Class<?>) {
            clazz = (Class<?>) obj;
            obj = null; // Static class
        } else {
            clazz = obj.getClass();
        }

        assert clazz.isAnnotationPresent(JavascriptObject.class) : "Class MUST be annotated with @JavascriptObject";

        _JavascriptObjectWrapper wrapper = new _JavascriptObjectWrapper(name, clazz, obj);
        this.objects.put(wrapper.id, wrapper);

        this.initScripts.add(
            String.format(
                "{\n" + ipc_object_fmt + "\n}",
                new JsonString(wrapper.id),
                new JsonString(wrapper.path),
                Rson.DEFAULT.toJson(wrapper.functions()),
                Rson.DEFAULT.toJson(wrapper.properties())
            )
        );

        // Look for sub-objects and register them.
        // Note that this recurses until there are no more sub-objects.
        for (Field f : _Reflection.getAllFields(clazz)) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            if (!f.getType().isAnnotationPresent(JavascriptObject.class)) continue;

            f.setAccessible(true);

            this.internal_defineObject(name + "." + f.getName(), f.get(obj));
        }

        return wrapper;
    }

}
