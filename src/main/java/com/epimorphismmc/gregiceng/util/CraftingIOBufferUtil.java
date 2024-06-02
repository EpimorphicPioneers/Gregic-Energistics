package com.epimorphismmc.gregiceng.util;

import com.epimorphismmc.gregiceng.common.machine.multiblock.part.CraftingIOBufferPartMachine;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class CraftingIOBufferUtil {

    public static List<Ingredient> copyIngredients(List<Ingredient> ingredients) {
        List<Ingredient> result = new ObjectArrayList<>(ingredients.size());
        for (Ingredient ingredient : ingredients) {
            result.add(ItemRecipeCapability.CAP.copyInner(ingredient));
        }
        return result;
    }

    public static List<FluidIngredient> copyFluidIngredients(List<FluidIngredient> ingredients) {
        List<FluidIngredient> result = new ObjectArrayList<>(ingredients.size());
        for (FluidIngredient ingredient : ingredients) {
            result.add(FluidRecipeCapability.CAP.copyInner(ingredient));
        }
        return result;
    }

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
