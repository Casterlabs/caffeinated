package co.casterlabs.caffeinated.pluginsdk.widgets;

import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.types.KoiEventType;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import lombok.With;

/**
 * See {@link https://heroicons.com} for icons.
 */
@Value
@AllArgsConstructor
@JsonClass(exposeAll = true)
public class WidgetDetails {
    private @With String namespace;

    private @With String icon; // https://heroicons.com
    private @With String friendlyName;
    private @With String localeBase;
    private @With WidgetDetailsCategory category;

    private @With WidgetType type;

    private @With(AccessLevel.PRIVATE) boolean showDemo;
    private @With(AccessLevel.PRIVATE) double demoAspectRatio;

    @With(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    @JsonField("requiredFeatures")
    private KoiIntegrationFeatures[] _requiredFeatures;

    @With(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    @JsonField("testEvents")
    private KoiEventType[] _testEvents;

    public WidgetDetails() {
        this.namespace = null;
        this.icon = "grid";
        this.friendlyName = null;
        this.localeBase = null;
        this.category = WidgetDetailsCategory.OTHER;
        this.type = WidgetType.WIDGET;
        this.showDemo = false;
        this.demoAspectRatio = 0;
        this._requiredFeatures = new KoiIntegrationFeatures[0];
        this._testEvents = new KoiEventType[0];
    }

    public WidgetDetails withShowDemo(boolean showDemo, double aspectRatio) {
        return this.withShowDemo(showDemo)
            .withDemoAspectRatio(aspectRatio);
    }

    public WidgetDetails withRequiredFeatures(KoiIntegrationFeatures... features) {
        return this.with_requiredFeatures(features);
    }

    public WidgetDetails withTestEvents(KoiEventType... types) {
        return this.with_testEvents(types);
    }

    public void validate() {
        assert this.namespace != null : "Namespace cannot be null";
        assert this.icon != null : "Icon cannot be null";
        assert this.friendlyName != null : "Friendly Name cannot be null";
        assert this._requiredFeatures != null : "Required Features cannot be null";
    }

    public KoiIntegrationFeatures[] getRequiredFeatures() {
        return this._requiredFeatures.clone();
    }

    public KoiEventType[] getTestEvents() {
        return this._testEvents.clone();
    }

    public static enum WidgetDetailsCategory {
        ALERTS,
        LABELS,
        INTERACTION,
        GOALS,
        OTHER;
    }

}
