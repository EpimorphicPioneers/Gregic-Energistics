package com.epimorphismmc.gregiceng;

import com.epimorphismmc.gregiceng.common.data.GERecipes;
import com.epimorphismmc.monomorphism.MOGTAddon;
import com.gregtechceu.gtceu.api.addon.GTAddon;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

@GTAddon
public class GEGTAddon extends MOGTAddon {
    public GEGTAddon() {
        super(GregicEng.MODID);
    }

    @Override
    public void initializeAddon() {

    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        GERecipes.init(provider);
    }
}
