package com.epimorphismmc.gregiceng.common.data;

import com.epimorphismmc.gregiceng.common.machine.multiblock.part.BufferPartMachine;
import com.epimorphismmc.gregiceng.common.machine.multiblock.part.appeng.AdvStockingBusPartMachine;
import com.epimorphismmc.gregiceng.common.machine.multiblock.part.appeng.AdvStockingHatchPartMachine;
import com.epimorphismmc.gregiceng.common.machine.multiblock.part.appeng.CraftingIOBufferPartMachine;
import com.epimorphismmc.gregiceng.common.machine.multiblock.part.appeng.CraftingIOSlavePartMachine;
import com.epimorphismmc.gregiceng.common.machine.multiblock.part.appeng.StockingBusPartMachine;
import com.epimorphismmc.gregiceng.common.machine.multiblock.part.appeng.StockingHatchPartMachine;
import com.epimorphismmc.gregiceng.config.GEConfigHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.BiFunction;

import static com.epimorphismmc.gregiceng.GregicEng.registrate;
import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.MULTI_HATCH_TIERS;

public class GEMachines {
    public static final PartAbility[] ALL_BUFFER_ABILITIES = new PartAbility[]{
        PartAbility.IMPORT_ITEMS,
        PartAbility.EXPORT_ITEMS,
        PartAbility.IMPORT_FLUIDS,
        PartAbility.EXPORT_FLUIDS,
    };

    static {
        registrate().creativeModeTab(() -> GECreativeModeTabs.MAIN);
    }

    public static final MachineDefinition STOCKING_BUS = registrate().machine("stocking_bus", StockingBusPartMachine::new)
        .tier(EV)
        .rotationState(RotationState.ALL)
        .abilities(PartAbility.IMPORT_ITEMS)
        .overlayTieredHullRenderer("stocking_bus")
        .tooltips(Component.translatable("block.gregiceng.stocking_bus.desc.0"))
        .register();

    public static final MachineDefinition STOCKING_HATCH = registrate().machine("stocking_hatch", StockingHatchPartMachine::new)
        .tier(EV)
        .rotationState(RotationState.ALL)
        .abilities(PartAbility.IMPORT_FLUIDS)
        .overlayTieredHullRenderer("stocking_hatch")
        .tooltips(Component.translatable("block.gregiceng.stocking_hatch.desc.0"))
        .register();

    public static final MachineDefinition ADV_STOCKING_BUS = registrate().machine("adv_stocking_bus", AdvStockingBusPartMachine::new)
        .tier(IV)
        .rotationState(RotationState.ALL)
        .abilities(PartAbility.IMPORT_ITEMS)
        .overlayTieredHullRenderer("adv_stocking_bus")
        .tooltips(Component.translatable("block.gregiceng.adv_stocking_bus.desc.0"))
        .register();

    public static final MachineDefinition ADV_STOCKING_HATCH = registrate().machine("adv_stocking_hatch", AdvStockingHatchPartMachine::new)
        .tier(IV)
        .rotationState(RotationState.ALL)
        .abilities(PartAbility.IMPORT_FLUIDS)
        .overlayTieredHullRenderer("adv_stocking_hatch")
        .tooltips(Component.translatable("block.gregiceng.adv_stocking_hatch.desc.0"))
        .register();

    public static final MachineDefinition CRAFTING_IO_BUFFER = registrate().machine("crafting_io_buffer", CraftingIOBufferPartMachine::new)
        .tier(LuV)
        .rotationState(RotationState.ALL)
        .abilities(GEConfigHolder.INSTANCE.enableMoreAbility ? ALL_BUFFER_ABILITIES : new PartAbility[] {PartAbility.IMPORT_ITEMS})
        .overlayTieredHullRenderer("crafting_io_buffer")
        .tooltips(Component.translatable("block.gregiceng.crafting_io_buffer.desc.0"))
        .register();

    public static final MachineDefinition CRAFTING_IO_SLAVE = registrate().machine("crafting_io_slave", CraftingIOSlavePartMachine::new)
        .tier(LuV)
        .rotationState(RotationState.ALL)
        .abilities(GEConfigHolder.INSTANCE.enableMoreAbility ? ALL_BUFFER_ABILITIES : new PartAbility[] {PartAbility.IMPORT_ITEMS})
        .overlayTieredHullRenderer("crafting_io_slave")
        .tooltips(Component.translatable("block.gregiceng.crafting_io_slave.desc.0"))
        .register();

    public static final MachineDefinition[] INPUT_BUFFER = registerTieredGEMachines("input_buffer",
            (holder, tier) -> new BufferPartMachine(holder, tier, IO.IN),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Input Buffer")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS)
                    .overlayTieredHullRenderer("buffer.import")
                    .tooltips(Component.translatable("block.gregiceng.input_buffer.desc"),
                            Component.translatable("gtceu.universal.tooltip.item_storage_capacity", (1 + Math.min(9, tier))*(1 + Math.min(9, tier))),
                            Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity_mult", 1 + Math.min(9, tier), FluidHatchPartMachine.getTankCapacity(BufferPartMachine.INITIAL_TANK_CAPACITY, tier)))
                    .register(),
            MULTI_HATCH_TIERS);

    public static final MachineDefinition[] OUTPUT_BUFFER = registerTieredGEMachines("output_buffer",
            (holder, tier) -> new BufferPartMachine(holder, tier, IO.OUT),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Output Buffer")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.EXPORT_ITEMS, PartAbility.EXPORT_FLUIDS)
                    .overlayTieredHullRenderer("buffer.export")
                    .tooltips(Component.translatable("block.gregiceng.output_buffer.desc"),
                            Component.translatable("gtceu.universal.tooltip.item_storage_capacity", (1 + Math.min(9, tier))*(1 + Math.min(9, tier))),
                            Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity_mult", 1 + Math.min(9, tier), FluidHatchPartMachine.getTankCapacity(BufferPartMachine.INITIAL_TANK_CAPACITY, tier)))
                    .register(),
            MULTI_HATCH_TIERS);

    public static void init() {

    }

    public static MachineDefinition[] registerTieredGEMachines(String name,
                                                               BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                               BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder,
                                                               int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[TIER_COUNT];
        for (int tier : tiers) {
            var register = registrate().machine(VN[tier].toLowerCase(Locale.ROOT) + "_" + name, holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }
}
