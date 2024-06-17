package com.epimorphismmc.gregiceng.config;

import com.epimorphismmc.gregiceng.GregicEng;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.format.ConfigFormats;

@Config(id = GregicEng.MODID)
public class GEConfigHolder {
    public static GEConfigHolder INSTANCE;

    public static void init() {
        if (INSTANCE == null) {
            INSTANCE = Configuration.registerConfig(GEConfigHolder.class, ConfigFormats.yaml())
                    .getConfigInstance();
        }
    }

    @Configurable
    @Configurable.Comment({
        "Let Buffer has more ability.",
        "When enabled it, Buffer will can used to assemble line and so on.",
        "Need restart Minecraft to apply."
    })
    public boolean enableMoreAbility = false;
}
