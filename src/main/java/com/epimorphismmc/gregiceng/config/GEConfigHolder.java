package com.epimorphismmc.gregiceng.config;

import com.epimorphismmc.gregiceng.GregicEng;
import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.format.ConfigFormats;

@Config(id = GregicEng.MODID)
public class GEConfigHolder {
    public static GEConfigHolder INSTANCE;

    public static void init() {
        if (INSTANCE == null) {
            INSTANCE = Configuration.registerConfig(GEConfigHolder.class, ConfigFormats.yaml()).getConfigInstance();
        }
    }


}
