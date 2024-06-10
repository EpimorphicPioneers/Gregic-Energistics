package com.epimorphismmc.gregiceng.common.machine.trait;

import com.epimorphismmc.gregiceng.common.machine.multiblock.part.appeng.CraftingIOBufferPartMachine;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.epimorphismmc.gregiceng.utils.GregicEngUtils.copyFluidIngredients;
import static com.epimorphismmc.gregiceng.utils.GregicEngUtils.copyIngredients;

public class IOBufferRecipeHandler extends MachineTrait {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER =
            new ManagedFieldHolder(IOBufferRecipeHandler.class);
    private ResourceLocation lockedRecipeId;
    private int lockedSlot;
    protected List<Runnable> listeners = new ArrayList<>();

    @Getter
    protected final IRecipeHandlerTrait<Ingredient> itemInputHandler;

    @Getter
    protected final IRecipeHandlerTrait<FluidIngredient> fluidInputHandler;

    @Getter
    protected final IRecipeHandlerTrait<Ingredient> itemOutputHandler;

    @Getter
    protected final IRecipeHandlerTrait<FluidIngredient> fluidOutputHandler;

    public IOBufferRecipeHandler(CraftingIOBufferPartMachine ioBuffer) {
        super(ioBuffer);
        this.itemInputHandler = new ItemInputHandler();
        this.fluidInputHandler = new FluidInputHandler();
        this.itemOutputHandler = new ItemOutputHandler();
        this.fluidOutputHandler = new FluidOutputHandler();
    }

    public void onChanged() {
        listeners.forEach(Runnable::run);
    }

    @Override
    public CraftingIOBufferPartMachine getMachine() {
        return (CraftingIOBufferPartMachine) super.getMachine();
    }

    public List<Ingredient> handleItemInner(
            GTRecipe recipe, List<Ingredient> left, boolean simulate) {
        var internalInv = getMachine().getInternalInventory();
        if (recipe.id.equals(lockedRecipeId) && lockedSlot >= 0) {
            left = internalInv[lockedSlot].handleItemInternal(left, simulate);
        } else {
            this.lockedRecipeId = recipe.id;
            List<Ingredient> contents = copyIngredients(left);
            for (int i = 0; i < internalInv.length; i++) {
                if (internalInv[i].isItemEmpty()) continue;
                contents = internalInv[i].handleItemInternal(contents, simulate);
                if (contents == null) {
                    this.lockedSlot = i;
                    return contents;
                }
                contents = copyIngredients(left);
            }
            this.lockedSlot = -1;
        }
        return left;
    }

    public List<FluidIngredient> handleFluidInner(
            GTRecipe recipe, List<FluidIngredient> left, boolean simulate) {
        var internalInv = getMachine().getInternalInventory();
        if (recipe.id.equals(lockedRecipeId) && lockedSlot >= 0) {
            left = internalInv[lockedSlot].handleFluidInternal(left, simulate);
        } else {
            this.lockedRecipeId = recipe.id;
            List<FluidIngredient> contents = copyFluidIngredients(left);
            for (int i = 0; i < internalInv.length; i++) {
                if (internalInv[i].isFluidEmpty()) continue;
                contents = internalInv[i].handleFluidInternal(contents, simulate);
                if (contents == null) {
                    this.lockedSlot = i;
                    return contents;
                }
                contents = copyFluidIngredients(left);
            }
            this.lockedSlot = -1;
        }
        return left;
    }

    @SuppressWarnings("rawtypes")
    public List<IRecipeHandlerTrait> getRecipeHandlers() {
        return List.of(fluidInputHandler, fluidOutputHandler, itemInputHandler, itemOutputHandler);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public class ItemInputHandler implements IRecipeHandlerTrait<Ingredient> {
        @Override
        public IO getHandlerIO() {
            return IO.IN;
        }

        @Override
        public ISubscription addChangedListener(Runnable listener) {
            listeners.add(listener);
            return () -> listeners.remove(listener);
        }

        @Override
        public List<Ingredient> handleRecipeInner(
                IO io,
                GTRecipe recipe,
                List<Ingredient> left,
                @Nullable String slotName,
                boolean simulate) {
            if (io != IO.IN) return left;
            var machine = getMachine();
            machine.getCircuitInventory().handleRecipeInner(io, recipe, left, slotName, simulate);
            left = handleItemInner(recipe, left, simulate);
            return left != null
                    ? machine.getShareInventory().handleRecipeInner(io, recipe, left, slotName, simulate)
                    : null;
        }

        @Override
        public List<Object> getContents() {
            return Arrays.stream(getMachine().getInternalInventory())
                    .map(CraftingIOBufferPartMachine.InternalSlot::getItemInputs)
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toList());
        }

        @Override
        public double getTotalContentAmount() {
            return Arrays.stream(getMachine().getInternalInventory())
                    .map(CraftingIOBufferPartMachine.InternalSlot::getItemInputs)
                    .flatMap(Arrays::stream)
                    .mapToLong(ItemStack::getCount)
                    .sum();
        }

        @Override
        public int getPriority() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isDistinct() {
            return true;
        }

        @Override
        public RecipeCapability<Ingredient> getCapability() {
            return ItemRecipeCapability.CAP;
        }

        @Override
        public void preWorking(IRecipeCapabilityHolder holder, IO io, GTRecipe recipe) {
            IRecipeHandlerTrait.super.preWorking(holder, io, recipe);
            lockedRecipeId = null;
        }
    }

    public class FluidInputHandler implements IRecipeHandlerTrait<FluidIngredient> {
        @Override
        public IO getHandlerIO() {
            return IO.IN;
        }

        @Override
        public ISubscription addChangedListener(Runnable listener) {
            listeners.add(listener);
            return () -> listeners.remove(listener);
        }

        @Override
        public List<FluidIngredient> handleRecipeInner(
                IO io,
                GTRecipe recipe,
                List<FluidIngredient> left,
                @Nullable String slotName,
                boolean simulate) {
            if (io != IO.IN) return left;
            left = handleFluidInner(recipe, left, simulate);
            return left != null
                    ? getMachine().getShareTank().handleRecipeInner(io, recipe, left, slotName, simulate)
                    : null;
        }

        @Override
        public List<Object> getContents() {
            return Arrays.stream(getMachine().getInternalInventory())
                    .map(CraftingIOBufferPartMachine.InternalSlot::getFluidInputs)
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toList());
        }

        @Override
        public double getTotalContentAmount() {
            return Arrays.stream(getMachine().getInternalInventory())
                    .map(CraftingIOBufferPartMachine.InternalSlot::getFluidInputs)
                    .flatMap(Arrays::stream)
                    .mapToLong(FluidStack::getAmount)
                    .sum();
        }

        @Override
        public int getPriority() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isDistinct() {
            return true;
        }

        @Override
        public RecipeCapability<FluidIngredient> getCapability() {
            return FluidRecipeCapability.CAP;
        }

        @Override
        public void preWorking(IRecipeCapabilityHolder holder, IO io, GTRecipe recipe) {
            IRecipeHandlerTrait.super.preWorking(holder, io, recipe);
            lockedRecipeId = null;
        }
    }

    public class ItemOutputHandler implements IRecipeHandlerTrait<Ingredient> {
        @Override
        public IO getHandlerIO() {
            return IO.OUT;
        }

        @Override
        public ISubscription addChangedListener(Runnable listener) {
            listeners.add(listener);
            return () -> listeners.remove(listener);
        }

        @Nullable @Override
        public List<Ingredient> handleRecipeInner(
                IO io,
                GTRecipe recipe,
                List<Ingredient> left,
                @Nullable String slotName,
                boolean simulate) {
            if (!getMachine().isWorkingEnabled() || io != IO.OUT) return left;
            if (!simulate) {
                for (Ingredient ingredient : left) {
                    var stack = ingredient.getItems()[0];
                    var key = AEItemKey.of(stack);
                    if (key == null) continue;
                    getMachine().getReturnBuffer().mergeLong(key, stack.getCount(), Long::sum);
                }
            }
            return null;
        }

        @Override
        public List<Object> getContents() {
            return Collections.emptyList();
        }

        @Override
        public double getTotalContentAmount() {
            return 1D;
        }

        @Override
        public int getPriority() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isDistinct() {
            return true;
        }

        @Override
        public RecipeCapability<Ingredient> getCapability() {
            return ItemRecipeCapability.CAP;
        }
    }

    public class FluidOutputHandler implements IRecipeHandlerTrait<FluidIngredient> {
        @Override
        public IO getHandlerIO() {
            return IO.OUT;
        }

        @Override
        public ISubscription addChangedListener(Runnable listener) {
            listeners.add(listener);
            return () -> listeners.remove(listener);
        }

        @Nullable @Override
        public List<FluidIngredient> handleRecipeInner(
                IO io,
                GTRecipe recipe,
                List<FluidIngredient> left,
                @Nullable String slotName,
                boolean simulate) {
            if (!getMachine().isWorkingEnabled() || io != IO.OUT) return left;
            if (!simulate) {
                for (FluidIngredient ingredient : left) {
                    if (ingredient.getAmount() <= 0) continue;
                    var stack = ingredient.getStacks()[0];
                    var key = AEFluidKey.of(stack.getFluid(), stack.getTag());
                    getMachine().getReturnBuffer().mergeLong(key, ingredient.getAmount(), Long::sum);
                }
            }
            return null;
        }

        @Override
        public List<Object> getContents() {
            return Collections.emptyList();
        }

        @Override
        public double getTotalContentAmount() {
            return 1D;
        }

        @Override
        public int getPriority() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isDistinct() {
            return true;
        }

        @Override
        public RecipeCapability<FluidIngredient> getCapability() {
            return FluidRecipeCapability.CAP;
        }
    }
}
