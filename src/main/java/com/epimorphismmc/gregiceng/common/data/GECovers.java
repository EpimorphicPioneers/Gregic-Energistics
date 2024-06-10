package com.epimorphismmc.gregiceng.common.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.client.renderer.cover.PumpCoverRenderer;
import com.gregtechceu.gtceu.common.cover.PumpCover;
import com.gregtechceu.gtceu.common.data.GTCovers;

public class GECovers {
    public static final CoverDefinition ELECTRIC_PUMP_MAX = GTCovers.register(
            "pump.max",
            (def, coverable, side) -> new PumpCover(def, coverable, side, GTValues.MAX),
            PumpCoverRenderer.INSTANCE);

    private GECovers() {
        /**/
    }

    public static void init() {
        /**/
    }
}
