package com.epimorphismmc.gregiceng.data.recipe.misc;

import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import com.epimorphismmc.gregiceng.common.data.GEMachines;
import com.epimorphismmc.gregiceng.data.recipe.GECraftingComponents;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.HV;
import static com.gregtechceu.gtceu.api.GTValues.VA;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;
import static com.gregtechceu.gtceu.data.recipe.CraftingComponent.GLASS;
import static com.gregtechceu.gtceu.data.recipe.CraftingComponent.HULL;
import static com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader.registerMachineRecipe;

public class MachineRecipeLoader {
    private MachineRecipeLoader() {/**/}

    public static void init(Consumer<FinishedRecipe> provider) {
        GECraftingComponents.init();
        registerMachineRecipe(provider, GEMachines.INPUT_BUFFER,
                "PG",
                "CM",
                'P', GECraftingComponents.BUFFER_PIPE,
                'M', HULL,
                'G', GLASS,
                'C', CustomTags.WOODEN_CHESTS);

        ASSEMBLER_RECIPES.recipeBuilder("crafting_input_buffer")
                .inputItems(GEMachines.INPUT_BUFFER[GTValues.LuV].asStack())
                .inputItems(AEParts.INTERFACE.stack(3))
                .inputItems(AEParts.PATTERN_PROVIDER.stack(3))
                .inputItems(AEItems.SPEED_CARD.stack(2))
                .inputItems(AEItems.CAPACITY_CARD.stack(2))
                .inputItems(CustomTags.LuV_CIRCUITS)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                .outputItems(GEMachines.CRAFTING_INPUT_BUFFER.asStack())
                .duration(300).EUt(VA[HV]).save(provider);
    }
}
