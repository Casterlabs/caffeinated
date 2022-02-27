package co.casterlabs.caffeinated.pluginsdk.widgets.settings;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import lombok.NonNull;

@Getter
@NonNull
@JsonClass(exposeAll = true)
public class WidgetSettingsLayout {
    private List<WidgetSettingsSection> sections = new LinkedList<>();
    private List<WidgetSettingsButton> buttons = new LinkedList<>();
    private boolean allowWidgetPreview;

    public WidgetSettingsLayout addButton(@NonNull WidgetSettingsButton button) {
        button.validate();
        this.buttons.add(button);
        return this;
    }

    public List<WidgetSettingsSection> getSections() {
        return Collections.unmodifiableList(this.sections);
    }

    public List<WidgetSettingsButton> getButtons() {
        return Collections.unmodifiableList(this.buttons);
    }

    public WidgetSettingsLayout setSections(@NonNull WidgetSettingsSection... section) {
        this.setSections(Arrays.asList(section));
        return this;
    }

    public WidgetSettingsLayout setSections(@NonNull List<WidgetSettingsSection> sections) {
        for (WidgetSettingsSection section : sections) {
            assert section == null : "NULL is not a valid WidgetSettingsSection.";
        }

        this.sections = new LinkedList<>(sections);
        return this;
    }

    public WidgetSettingsLayout addSection(@NonNull WidgetSettingsSection section) {
        this.sections.add(section);
        return this;
    }

    public WidgetSettingsLayout removeSection(@NonNull WidgetSettingsSection section) {
        this.sections.remove(section);
        return this;
    }

}
