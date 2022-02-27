package co.casterlabs.caffeinated.pluginsdk.widgets.settings;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@NonNull
@RequiredArgsConstructor
@JsonClass(exposeAll = true)
public class WidgetSettingsSection {
    private final String id;
    private final String name;
    private List<WidgetSettingsItem> items = new LinkedList<>();

    public List<WidgetSettingsItem> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    public WidgetSettingsSection setItems(@NonNull WidgetSettingsItem... items) {
        this.setItems(Arrays.asList(items));
        return this;
    }

    public WidgetSettingsSection setItems(@NonNull List<WidgetSettingsItem> items) {
        for (WidgetSettingsItem item : items) {
            if (item == null) {
                assert item == null : "NULL is not a valid WidgetSettingsItem.";
            }
        }

        this.items = new LinkedList<>(items);
        return this;
    }

    public WidgetSettingsSection addItem(@NonNull WidgetSettingsItem item) {
        this.items.add(item);
        return this;
    }

    public WidgetSettingsSection removeItem(@NonNull WidgetSettingsItem item) {
        this.items.remove(item);
        return this;
    }

}
