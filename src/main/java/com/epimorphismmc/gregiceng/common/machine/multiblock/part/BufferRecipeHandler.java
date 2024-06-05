package com.epimorphismmc.gregiceng.common.machine.multiblock.part;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import com.epimorphismmc.monomorphism.ae2.AEUtils;
import com.epimorphismmc.monomorphism.recipe.MORecipeHelper;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.epimorphismmc.gregiceng.util.CraftingIOBufferUtil.copyFluidIngredients;
import static com.epimorphismmc.gregiceng.util.CraftingIOBufferUtil.copyIngredients;

public class BufferRecipeHandler extends MachineTrait {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(BufferRecipeHandler.class);
    @Getter
    protected final IRecipeHandlerTrait<Ingredient> itemInputHandler;
    @Getter
    protected final IRecipeHandlerTrait<FluidIngredient> fluidInputHandler;
    @Getter
    protected final IRecipeHandlerTrait<Ingredient> itemOutputHandler;
    @Getter
    protected final IRecipeHandlerTrait<FluidIngredient> fluidOutputHandler;
    protected List<Runnable> listeners = new ArrayList<>();
    protected Table<GTRecipeKey, AEFluidKey, Long> returnFluidTable = HashBasedTable.create();
    protected Table<GTRecipeKey, AEItemKey, Long> returnItemTable = HashBasedTable.create();
    private ResourceLocation lockedRecipeId;
    private int lockedSlot;
    private boolean hasCached;
    private boolean isOutputting;

    public BufferRecipeHandler(CraftingIOBufferPartMachine ioBuffer) {
        super(ioBuffer);
        this.itemInputHandler = new ItemInputHandler();
        this.fluidInputHandler = new FluidInputHandler();
        this.itemOutputHandler = new ItemOutputHandler();
        this.fluidOutputHandler = new FluidOutputHandler();
    }

    public void onChanged() {
        listeners.forEach(Runnable::run);
    }

    public void clearCache() {
        returnFluidTable.clear();
        returnItemTable.clear();
    }

    @Override
    public CraftingIOBufferPartMachine getMachine() {
        return (CraftingIOBufferPartMachine) super.getMachine();
    }

    public List<Ingredient> handleItemInner(GTRecipe recipe, List<Ingredient> left, boolean simulate) {
        if (recipe.id.equals(lockedRecipeId) && lockedSlot >= 0) {
            left = getMachine().internalInventory[lockedSlot].handleItemInternal(left, simulate);
            if (!hasCached && left == null && !simulate) {
                cacheRecipeOutput(recipe);
            }
        } else {
            this.lockedRecipeId = recipe.id;
            List<Ingredient> contents = copyIngredients(left);
            for (int i = 0; i < getMachine().internalInventory.length; i++) {
                if (getMachine().internalInventory[i].isItemEmpty()) continue;
                contents = getMachine().internalInventory[i].handleItemInternal(contents, simulate);
                if (contents == null) {
                    if (!hasCached && !simulate) {
                        cacheRecipeOutput(recipe);
                    }
                    this.lockedSlot = i;
                    return contents;
                }
                contents = copyIngredients(left);
            }
            this.lockedSlot = -1;
        }
        return left;
    }

    public List<FluidIngredient> handleFluidInner(GTRecipe recipe, List<FluidIngredient> left, boolean simulate) {
        if (recipe.id.equals(lockedRecipeId) && lockedSlot >= 0) {
            left = getMachine().internalInventory[lockedSlot].handleFluidInternal(left, simulate);
            if (!hasCached && left == null && !simulate) {
                cacheRecipeOutput(recipe);
            }
        } else {
            this.lockedRecipeId = recipe.id;
            List<FluidIngredient> contents = copyFluidIngredients(left);
            for (int i = 0; i < getMachine().internalInventory.length; i++) {
                if (getMachine().internalInventory[i].isFluidEmpty()) continue;
                contents = getMachine().internalInventory[i].handleFluidInternal(contents, simulate);
                if (contents == null) {
                    if (!hasCached && !simulate) {
                        cacheRecipeOutput(recipe);
                    }
                    this.lockedSlot = i;
                    return contents;
                }
                contents = copyFluidIngredients(left);
            }
            this.lockedSlot = -1;
        }
        return left;
    }

    private void cacheRecipeOutput(GTRecipe recipe) {
        var recipeKey = GTRecipeKey.create(recipe);
        var rowItem = returnItemTable.row(recipeKey);
        var rowFluid = returnFluidTable.row(recipeKey);

        boolean isEmpty = rowItem.isEmpty();
        for (ItemStack stack : MORecipeHelper.getOutputItem(recipe)) {
            if (!isEmpty) {
                for (var entry : rowItem.entrySet()) {
                    if (entry.getKey().matches(stack)) {
                        entry.setValue(entry.getValue() + stack.getCount());
                    }
                }
            } else {
                var key = AEItemKey.of(stack.getItem(), stack.getTag());
                returnItemTable.put(recipeKey, key, (long) stack.getCount());
            }
        }

        isEmpty = rowFluid.isEmpty();
        for (FluidStack stack : MORecipeHelper.getOutputFluid(recipe)) {
            if (!isEmpty) {
                for (var entry : rowFluid.entrySet()) {
                    if (AEUtils.matches(entry.getKey(), stack)) {
                        entry.setValue(entry.getValue() + stack.getAmount());
                    }
                }
            } else {
                var key = AEFluidKey.of(stack.getFluid(), stack.getTag());
                returnFluidTable.put(recipeKey, key, stack.getAmount());
            }
        }
        this.hasCached = true;
    }

    public List<IRecipeHandlerTrait> getRecipeHandlers() {
        return List.of(fluidInputHandler, fluidOutputHandler, itemInputHandler, itemOutputHandler);
    }

    public void saveCustomPersistedData(CompoundTag tag) {
        saveItemTable(tag);
        saveFluidTable(tag);
    }

    public void loadCustomPersistedData(CompoundTag tag) {
        loadItemTable(tag);
        loadFluidTable(tag);
    }

    private void saveItemTable(CompoundTag tag) {
        var tableTag = new ListTag();
        for (Map.Entry<AEItemKey, Map<GTRecipeKey, Long>> entry : returnItemTable.columnMap().entrySet()) {
            var mapTag = new ListTag();
            entry.getValue().forEach((key, value) -> {
                var entryTag = new CompoundTag();
                entryTag.put("key", key.toTag());
                entryTag.putLong("value", value);
                mapTag.add(entryTag);
            });
            var entryTag = new CompoundTag();
            entryTag.put("key", entry.getKey().toTag());
            entryTag.put("value", mapTag);
            tableTag.add(entryTag);
        }
        tag.put("returnItemTable", tableTag);
    }

    private void saveFluidTable(CompoundTag tag) {
        var tableTag = new ListTag();
        for (Map.Entry<AEFluidKey, Map<GTRecipeKey, Long>> entry : returnFluidTable.columnMap().entrySet()) {
            var mapTag = new ListTag();
            entry.getValue().forEach((key, value) -> {
                var entryTag = new CompoundTag();
                entryTag.put("key", key.toTag());
                entryTag.putLong("value", value);
                mapTag.add(entryTag);
            });
            var entryTag = new CompoundTag();
            entryTag.put("key", entry.getKey().toTag());
            entryTag.put("value", mapTag);
            tableTag.add(entryTag);
        }
        tag.put("returnFluidTable", tableTag);
    }

    private void loadItemTable(CompoundTag tag) {
        var tableTag = tag.getList("returnItemTable", Tag.TAG_COMPOUND);
        for (int i = 0; i < tableTag.size(); i++) {
            var columnTag = tableTag.getCompound(i);
            var itemKey = AEItemKey.fromTag(columnTag.getCompound("key"));
            var mapTag = columnTag.getList("value", Tag.TAG_COMPOUND);
            for (int j = 0; j < mapTag.size(); j++) {
                var entryTag = mapTag.getCompound(j);
                returnItemTable.put(GTRecipeKey.fromTag(entryTag.getCompound("key")), itemKey, entryTag.getLong("value"));
            }
        }
    }

    private void loadFluidTable(CompoundTag tag) {
        var tableTag = tag.getList("returnFluidTable", Tag.TAG_COMPOUND);
        for (int i = 0; i < tableTag.size(); i++) {
            var columnTag = tableTag.getCompound(i);
            var fluidKey = AEFluidKey.fromTag(columnTag.getCompound("key"));
            var mapTag = columnTag.getList("value", Tag.TAG_COMPOUND);
            for (int j = 0; j < mapTag.size(); j++) {
                var entryTag = mapTag.getCompound(j);
                returnFluidTable.put(GTRecipeKey.fromTag(entryTag.getCompound("key")), fluidKey, entryTag.getLong("value"));
            }
        }
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
        public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left, @Nullable String slotName, boolean simulate) {
            if (io != IO.IN) return left;
            isOutputting = false;
            getMachine().circuitInventory.handleRecipeInner(io, recipe, left, slotName, simulate);
            left = handleItemInner(recipe, left, simulate);
            return left != null ? getMachine().shareInventory.handleRecipeInner(io, recipe, left, slotName, simulate) : null;
        }

        @Override
        public List<Object> getContents() {
            return Arrays.stream(getMachine().internalInventory)
                    .map(CraftingIOBufferPartMachine.InternalSlot::getItemInputs)
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toList());
        }

        @Override
        public double getTotalContentAmount() {
            return Arrays.stream(getMachine().internalInventory)
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
            hasCached = false;
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
        public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left, @Nullable String slotName, boolean simulate) {
            if (io != IO.IN) return left;
            isOutputting = false;
            left = handleFluidInner(recipe, left, simulate);
            return left != null ? getMachine().shareTank.handleRecipeInner(io, recipe, left, slotName, simulate) : null;
        }

        @Override
        public List<Object> getContents() {
            return Arrays.stream(getMachine().internalInventory)
                    .map(CraftingIOBufferPartMachine.InternalSlot::getFluidInputs)
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toList());
        }

        @Override
        public double getTotalContentAmount() {
            return Arrays.stream(getMachine().internalInventory)
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
            hasCached = false;
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

        @Override
        public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left, @Nullable String slotName, boolean simulate) {
            if (io != IO.OUT) return left;
            if (!isOutputting) return null;

            var recipeKey = GTRecipeKey.create(recipe);
            if (!returnItemTable.containsRow(recipeKey)) return left;
            Iterator<Ingredient> iterator = left.iterator();
            var returnItemMap = returnItemTable.row(recipeKey);
            while (iterator.hasNext()) {
                Ingredient ingredient = iterator.next();
                ItemStack[] ingredientStacks = ingredient.getItems();
                SLOT_LOOKUP:
                for (ItemStack ingredientStack : ingredientStacks) {
                    for (var entry : returnItemMap.entrySet()) {
                        var key = entry.getKey();
                        long amount = entry.getValue();
                        int count = ingredientStack.getCount();
                        if (key.matches(ingredientStack)) {
                            if (!simulate) {
                                getMachine().returnBuffer.mergeLong(key, count, Long::sum);
                                if (amount <= count) {
                                    returnItemMap.remove(key);
                                } else {
                                    entry.setValue(amount - count);
                                }
                            }
                            iterator.remove();
                            break SLOT_LOOKUP;
                        }
                    }
                }
            }
            return left.isEmpty() ? null : left;
        }

        @Override
        public List<Object> getContents() {
            return Collections.emptyList();
        }

        @Override
        public double getTotalContentAmount() {
            return 0D;
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
        public void postWorking(IRecipeCapabilityHolder holder, IO io, GTRecipe recipe) {
            IRecipeHandlerTrait.super.postWorking(holder, io, recipe);
            isOutputting = true;
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

        @Override
        public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left, @Nullable String slotName, boolean simulate) {
            if (io != IO.OUT) return left;
            if (!isOutputting) return null;

            var recipeKey = GTRecipeKey.create(recipe);
            if (!returnFluidTable.containsRow(recipeKey)) return left;
            Iterator<FluidIngredient> iterator = left.iterator();
            var returnFluidMap = returnFluidTable.row(recipeKey);
            while (iterator.hasNext()) {
                FluidIngredient ingredient = iterator.next();
                FluidStack[] ingredientStacks = ingredient.getStacks();
                SLOT_LOOKUP:
                for (FluidStack ingredientStack : ingredientStacks) {
                    for (var entry : returnFluidMap.entrySet()) {
                        var key = entry.getKey();
                        long amount = entry.getValue();
                        long count = ingredientStack.getAmount();
                        if (AEUtils.matches(key, ingredientStack)) {
                            if (!simulate) {
                                getMachine().returnBuffer.mergeLong(key, count, Long::sum);
                                if (amount <= count) {
                                    returnFluidMap.remove(key);
                                } else {
                                    entry.setValue(amount - count);
                                }
                            }
                            iterator.remove();
                            break SLOT_LOOKUP;
                        }
                    }
                }
            }
            return left.isEmpty() ? null : left;
        }

        @Override
        public List<Object> getContents() {
            return Collections.emptyList();
        }

        @Override
        public double getTotalContentAmount() {
            return 0D;
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
        public void postWorking(IRecipeCapabilityHolder holder, IO io, GTRecipe recipe) {
            IRecipeHandlerTrait.super.postWorking(holder, io, recipe);
            isOutputting = true;
        }
    }
}
