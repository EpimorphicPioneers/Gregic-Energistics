package com.epimorphismmc.gregiceng.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.data.recipe.CraftingComponent;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class GECraftingComponents {
    public static CraftingComponent.Component BUFFER_PIPE;

    public static void init() {
        BUFFER_PIPE = new CraftingComponent.Component(Stream.of(new Object[][] {
                    {4, new UnificationEntry(TagPrefix.pipeNonupleFluid, Titanium)},
                    {5, new UnificationEntry(TagPrefix.pipeNonupleFluid, TungstenSteel)},
                    {6, new UnificationEntry(TagPrefix.pipeNonupleFluid, NiobiumTitanium)},
                    {7, new UnificationEntry(TagPrefix.pipeNonupleFluid, Iridium)},
                    {8, new UnificationEntry(TagPrefix.pipeNonupleFluid, Naquadah)},
                    {GTValues.FALLBACK, new UnificationEntry(TagPrefix.pipeNonupleFluid, Neutronium)},
                })
                .collect(Collectors.toMap(data -> (Integer) data[0], data -> data[1])));
    }
}
