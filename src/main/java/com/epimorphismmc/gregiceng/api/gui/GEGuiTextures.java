package com.epimorphismmc.gregiceng.api.gui;

import com.epimorphismmc.gregiceng.GregicEng;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

public class GEGuiTextures {
    public static final ResourceTexture REFUND_OVERLAY = createTexture("overlay/refund_overlay.png");
    public static final ResourceTexture PATTERN_OVERLAY = createTexture("overlay/pattern_overlay.png");

    private static ResourceTexture createTexture(String imageLocation) {
        return new ResourceTexture("%s:textures/gui/%s".formatted(GregicEng.MODID, imageLocation));
    }

    private static ResourceBorderTexture createBorderTexture(String imageLocation, int imageWidth, int imageHeight, int cornerWidth, int cornerHeight) {
        return new ResourceBorderTexture("%s:textures/gui/%s".formatted(GregicEng.MODID, imageLocation), imageWidth, imageHeight, cornerWidth, cornerHeight);
    }
}
