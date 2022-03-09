package co.casterlabs.caffeinated.app.ui;

// This is so nasty, I love it.
public class UIDocksPlugin {
} /*extends CaffeinatedPlugin {
  private static BridgeShim bridge;
  
  @SuppressWarnings("deprecation")
  @Override
  public void onInit() {
  this.getPlugins().registerWidget(this, StreamChatDock.DETAILS, StreamChatDock.class);
  this.getPlugins().registerWidget(this, StreamViewersDock.DETAILS, StreamViewersDock.class);
  
  bridge = new BridgeShim();
  bridge.mergeWith(CaffeinatedApp.getInstance().getAppBridge());
  }
  
  @Override
  public void onClose() {
  
  }
  
  @Override
  public @NonNull String getName() {
  return "Casterlabs UI Docks";
  }
  
  @Override
  public @NonNull String getId() {
  return "co.casterlabs.uidocks";
  }
  
  public static class StreamChatDock extends Widget {
  public static final WidgetDetails DETAILS = new WidgetDetails()
  .withNamespace("co.casterlabs.dock.stream_chat")
  .withIcon("message-circle")
  .withType(WidgetType.DOCK)
  .withFriendlyName("Stream Chat");
  
  private static final List<String> EVENT_TYPES = Arrays.asList(
  "koi:chat_send",
  "koi:chat_delete",
  "koi:chat_upvote",
  "auth:update"
  );
  
  @SneakyThrows
  @Override
  public void onInit() {
  // NEVER do this.
  WidgetHandle handle = ReflectionLib.getValue(this, "$handle");
  String newFormat = "https://studio.casterlabs.co/popout/stream-chat?pluginId=%s&widgetId=%s&authorization=%s&port=%d&mode=%s";
  
  ReflectionLib.setValue(handle, "urlFormat", newFormat);
  }
  
  @Override
  public void onNewInstance(@NonNull WidgetInstance instance) {
  for (String event : EVENT_TYPES) {
  instance.on(event, (c) -> {
  bridge.fireEvent(event, c);
  });
  }
  }
  
  }
  
  public static class StreamViewersDock extends Widget {
  public static final WidgetDetails DETAILS = new WidgetDetails()
  .withNamespace("co.casterlabs.dock.stream_viewers")
  .withIcon("eye")
  .withType(WidgetType.DOCK)
  .withFriendlyName("Stream Viewers");
  
  @SneakyThrows
  @Override
  public void onInit() {
  // NEVER do this.
  WidgetHandle handle = ReflectionLib.getValue(this, "$handle");
  String newFormat = "https://studio.casterlabs.co/popout/stream-viewers?pluginId=%s&widgetId=%s&authorization=%s&port=%d&mode=%s";
  
  ReflectionLib.setValue(handle, "urlFormat", newFormat);
  }
  
  }
  
  public class BridgeShim extends WebviewBridge {
  
  @Override
  public Map<String, BridgeValue<?>> getQueryData() {
  return super.getQueryData();
  }
  
  public void fireEvent(String type, JsonElement data) {
  this.onEvent.accept(type, data.getAsObject());
  }
  
  @Override
  protected void emit0(@NonNull String type, @NonNull JsonElement data) {
  for (Widget w : getWidgets()) {
  w.broadcastToAll(type, data);
  }
  }
  
  @Override
  protected void eval0(@NonNull String script) {
  // for (Widget w : getWidgets()) {
  // w.broadcastToAll("__eval", new JsonString(script));
  // }
  }
  
  @Override
  protected void invokeCallback0(@NonNull String invokeId, @NonNull JsonArray arguments) {
  // TODO
  }
  
  @Override
  protected void removeCallback0(@NonNull String arg0) {
  // TODO Auto-generated method stub
  
  }
  
  }
  }
  */
