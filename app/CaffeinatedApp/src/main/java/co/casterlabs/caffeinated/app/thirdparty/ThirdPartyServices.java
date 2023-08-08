package co.casterlabs.caffeinated.app.thirdparty;

import java.util.Arrays;
import java.util.List;

import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;

public class ThirdPartyServices {

    public static List<CaffeinatedPlugin> init() {
        return Arrays.asList(
            new StreamlabsServicePlugin(),
            new KofiServicePlugin()
        );
    }

}
