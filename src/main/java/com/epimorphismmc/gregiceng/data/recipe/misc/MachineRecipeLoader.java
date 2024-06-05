package com.epimorphismmc.gregiceng.data.recipe.misc;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import com.epimorphismmc.gregiceng.common.data.GEMachines;
import com.epimorphismmc.gregiceng.data.recipe.GECraftingComponents;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.epimorphismmc.gregiceng.common.data.GEMachines.INPUT_BUFFER;
import static com.epimorphismmc.gregiceng.common.data.GEMachines.OUTPUT_BUFFER;
import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.MULTI_HATCH_TIERS;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;
import static com.gregtechceu.gtceu.data.recipe.CraftingComponent.GLASS;
import static com.gregtechceu.gtceu.data.recipe.CraftingComponent.HULL;
import static com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader.registerMachineRecipe;

public class MachineRecipeLoader {
    private MachineRecipeLoader() {/**/}

    public static void init(Consumer<FinishedRecipe> provider) {
        GECraftingComponents.init();
        registerMachineRecipe(provider, INPUT_BUFFER,
                "PG",
                "CM",
                'P', GECraftingComponents.BUFFER_PIPE,
                'M', HULL,
                'G', GLASS,
                'C', CustomTags.WOODEN_CHESTS);

        registerMachineRecipe(provider, OUTPUT_BUFFER,
                "MG",
                "CP",
                'P', GECraftingComponents.BUFFER_PIPE,
                'M', HULL,
                'G', GLASS,
                'C', CustomTags.WOODEN_CHESTS);

        for (int tier : MULTI_HATCH_TIERS) {
            var tierName = VN[tier].toLowerCase();

            var input_buffer = INPUT_BUFFER[tier];
            var output_buffer = OUTPUT_BUFFER[tier];

            VanillaRecipeHelper.addShapedRecipe(
                    provider, "buffer_output_to_input_" + tierName,
                    input_buffer.asStack(), "d", "B",
                    'B', output_buffer.asStack()
            );
            VanillaRecipeHelper.addShapedRecipe(
                    provider, "buffer_input_to_output_" + tierName,
                    output_buffer.asStack(), "d", "B",
                    'B', input_buffer.asStack()
            );
        }

        ASSEMBLER_RECIPES.recipeBuilder("crafting_io_buffer")
                .inputItems(INPUT_BUFFER[GTValues.LuV].asStack())
                .inputItems(OUTPUT_BUFFER[GTValues.LuV].asStack())
                .inputItems(AEBlocks.INTERFACE.stack(3))
                .inputItems(AEBlocks.PATTERN_PROVIDER.stack(3))
                .inputItems(AEItems.SPEED_CARD.stack(2))
                .inputItems(AEItems.CAPACITY_CARD.stack(2))
                .inputItems(CustomTags.LuV_CIRCUITS)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                .outputItems(GEMachines.CRAFTING_IO_BUFFER.asStack())
                .duration(300).EUt(VA[HV]).save(provider);
    }
}
