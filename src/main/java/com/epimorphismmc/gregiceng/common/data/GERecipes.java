package com.epimorphismmc.gregiceng.common.data;

import com.epimorphismmc.gregiceng.data.recipe.misc.MachineRecipeLoader;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

public class GERecipes {

    private GERecipes() {/**/}

    public static void init(Consumer<FinishedRecipe> provider) {
        MachineRecipeLoader.init(provider);
    }
}
