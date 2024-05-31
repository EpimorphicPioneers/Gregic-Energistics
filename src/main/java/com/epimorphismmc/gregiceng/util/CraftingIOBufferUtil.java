package com.epimorphismmc.gregiceng.util;

import com.epimorphismmc.gregiceng.common.machine.multiblock.part.CraftingIOBufferPartMachine;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class CraftingIOBufferUtil {
    public static Pair<Object2LongOpenHashMap<Item>, Object2LongOpenHashMap<Fluid>> mergeInternalSlot(
        CraftingIOBufferPartMachine.InternalSlot[] internalSlots
    ) {
        Object2LongOpenHashMap<Item> items = new Object2LongOpenHashMap<>();
        Object2LongOpenHashMap<Fluid> fluids = new Object2LongOpenHashMap<>();
        for (CraftingIOBufferPartMachine.InternalSlot internalSlot : internalSlots) {
            for (ItemStack stack : internalSlot.getItemInputs()) {
                items.addTo(stack.getItem(), stack.getCount());
            }
            for (FluidStack stack : internalSlot.getFluidInputs()) {
                fluids.addTo(stack.getFluid(), stack.getAmount());
            }
        }
        return new ImmutablePair<>(items, fluids);
    }
}
