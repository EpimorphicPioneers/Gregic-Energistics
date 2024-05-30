package com.epimorphismmc.gregiceng.common.data;

import com.epimorphismmc.gregiceng.common.machine.multiblock.part.CraftingInputBufferMachine;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import net.minecraft.network.chat.Component;

import static com.epimorphismmc.gregiceng.GregicEng.registrate;
import static com.gregtechceu.gtceu.api.GTValues.UV;

public class GEMachines {

    static {
        registrate().creativeModeTab(() -> GECreativeModeTabs.MAIN);
    }

    public final static MachineDefinition CRAFTING_INPUT_BUFFER = registrate().machine("crafting_input_buffer", CraftingInputBufferMachine::new)
            .tier(UV)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS, PartAbility.EXPORT_ITEMS, PartAbility.EXPORT_FLUIDS)
            .overlayTieredHullRenderer("crafting_input_buffer")
            .tooltips(Component.translatable("block.gregiceng.crafting_input_buffer.desc.0"))
            .register();

    public static void init() {

    }

}
