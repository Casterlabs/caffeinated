package co.casterlabs.caffeinated.pluginsdk.widgets;

public enum WidgetType {
    /**
     * Regular Widget
     */
    WIDGET,

    /**
     * Embeddable in the Dashboard
     */
    DOCK,

    /**
     * Has it's own tab in the sidebar.
     */
    APPLET,

    /**
     * Has it's own tab on the settings page.
     */
    SETTINGS_APPLET

}
