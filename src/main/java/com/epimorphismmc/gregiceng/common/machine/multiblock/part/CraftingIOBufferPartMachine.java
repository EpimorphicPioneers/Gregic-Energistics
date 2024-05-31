package com.epimorphismmc.gregiceng.common.machine.multiblock.part;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.GridHelper;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.*;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.crafting.pattern.ProcessingPatternItem;
import com.epimorphismmc.gregiceng.GregicEng;
import com.epimorphismmc.gregiceng.api.gui.GEGuiTextures;
import com.epimorphismmc.monomorphism.ae2.AEUtils;
import com.epimorphismmc.monomorphism.ae2.MEPartMachine;
import com.epimorphismmc.monomorphism.machine.fancyconfigurator.ButtonConfigurator;
import com.epimorphismmc.monomorphism.machine.fancyconfigurator.InventoryFancyConfigurator;
import com.epimorphismmc.monomorphism.machine.fancyconfigurator.TankFancyConfigurator;
import com.epimorphismmc.monomorphism.recipe.MORecipeHelper;
import com.epimorphismmc.monomorphism.utility.MONBTUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class CraftingIOBufferPartMachine extends MEPartMachine implements ICraftingProvider {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CraftingIOBufferPartMachine.class, MEPartMachine.MANAGED_FIELD_HOLDER);
    private static final int MAX_PATTERN_COUNT = 6 * 9;

    @Getter
    @Persisted
    private final ItemStackTransfer patternInventory = new ItemStackTransfer(MAX_PATTERN_COUNT);
    @Getter
    @Persisted
    protected final NotifiableItemStackHandler circuitInventory;
    @Getter
    @Persisted
    protected final NotifiableItemStackHandler shareInventory;
    @Getter
    @Persisted
    protected final NotifiableFluidTank shareTank;
    @Persisted
    private final InternalSlot[] internalInventory = new InternalSlot[MAX_PATTERN_COUNT];
    private final BiMap<IPatternDetails, InternalSlot> patternDetailsPatternSlotMap = HashBiMap.create(MAX_PATTERN_COUNT);
    @Persisted
    private ResourceLocation lockedRecipeId;
    @Persisted
    private int lockedSlot;
    private boolean isOutputting;
    private boolean needPatternSync = true;
    protected List<Runnable> listeners = new ArrayList<>();
    protected Object2LongOpenHashMap<AEFluidKey> returnFluidMap = new Object2LongOpenHashMap<>();
    protected Object2LongOpenHashMap<AEItemKey> returnItemMap = new Object2LongOpenHashMap<>();
    protected Object2LongOpenHashMap<AEKey> returnBuffer = new Object2LongOpenHashMap<>();

    @Nullable
    protected TickableSubscription updateSubs;

    public CraftingIOBufferPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, GTValues.LuV, IO.BOTH, args);
        this.patternInventory.setFilter(stack -> stack.getItem() instanceof ProcessingPatternItem);
        for (int i = 0; i < this.internalInventory.length; i++) {
            this.internalInventory[i] = new InternalSlot();
        }
        getMainNode().addService(ICraftingProvider.class, this);
        this.circuitInventory = new NotifiableItemStackHandler(this, 1, IO.IN, IO.NONE).setFilter(IntCircuitBehaviour::isIntegratedCircuit);
        this.shareInventory = new NotifiableItemStackHandler(this, 9, IO.IN, IO.NONE);
        this.shareTank = new NotifiableFluidTank(this, 9, 8 * FluidHelper.getBucket(), IO.IN, IO.NONE);
    }

    @Override
    public boolean afterWorking(IWorkableMultiController controller) {
        this.isOutputting = true;
        this.lockedRecipeId = null;
        return super.afterWorking(controller);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(1, () -> {
                for (int i = 0; i < patternInventory.getSlots(); i++) {
                    var pattern = patternInventory.getStackInSlot(i);
                    var patternDetails = PatternDetailsHelper.decodePattern(pattern, getLevel());
                    if (patternDetails != null) {
                        this.patternDetailsPatternSlotMap.put(patternDetails, this.internalInventory[i]);
                    }
                }
            }));
        }
    }

    protected void updateSubscription() {
        if (getLevel() != null && GridHelper.getNodeHost(getLevel(), getPos().relative(getFrontFacing())) != null) {
            updateSubs = subscribeServerTick(updateSubs, this::update);
        } else if (updateSubs != null) {
            updateSubs.unsubscribe();
            updateSubs = null;
        }
    }

    protected void update() {
        if (needPatternSync) {
            ICraftingProvider.requestUpdate(getMainNode());
            this.needPatternSync = false;
        }

        if (!shouldSyncME()) return;

        if (updateMEStatus() && !this.returnBuffer.isEmpty()) {
            MEStorage aeNetwork = this.getMainNode().getGrid().getStorageService().getInventory();
            for (var entry : returnBuffer.object2LongEntrySet()) {
                var key = entry.getKey();
                var amount = entry.getLongValue();
                long inserted = StorageHelper.poweredInsert(
                        getMainNode().getGrid().getEnergyService(), aeNetwork,
                        key, amount,
                        actionSource);
                if (inserted >= amount) {
                    returnBuffer.removeLong(key);
                } else {
                    returnBuffer.put(key, amount - inserted);
                }
            }
        }
    }

    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        returnFluidMap.clear();
        returnItemMap.clear();
    }

    public List<Ingredient> handleItemInner(GTRecipe recipe, List<Ingredient> left, boolean simulate) {
        if (recipe.id.equals(lockedRecipeId) && lockedSlot >= 0) {
            left = internalInventory[lockedSlot].handleItemInternal(left, simulate);
            if (left == null && !simulate) {
                cacheItemOutput(recipe);
            }
            return left;
        } else {
            this.lockedRecipeId = recipe.id;
            List<Ingredient> contents = copyIngredients(left);
            for (int i = 0; i < internalInventory.length; i++) {
                if (internalInventory[i].isItemEmpty()) continue;
                contents = internalInventory[i].handleItemInternal(contents, simulate);
                if (contents == null) {
                    if (!simulate) cacheItemOutput(recipe);
                    this.lockedSlot = i;
                    return contents;
                }
                contents = copyIngredients(left);
            }
            this.lockedSlot = -1;
            return left;
        }
    }

    private void cacheItemOutput(GTRecipe recipe) {
        this.returnItemMap.clear();
        for (ItemStack stack : MORecipeHelper.getOutputItem(recipe)) {
            var key = AEItemKey.of(stack.getItem(), stack.getTag());
            returnItemMap.mergeLong(key, stack.getCount(), Long::sum);
        }
    }

    public List<FluidIngredient> handleFluidInner(GTRecipe recipe, List<FluidIngredient> left, boolean simulate) {
        if (recipe.id.equals(lockedRecipeId) && lockedSlot >= 0) {
            left = internalInventory[lockedSlot].handleFluidInternal(left, simulate);
            if (left == null && !simulate) {
                cacheFluidOutput(recipe);
            }
            return left;
        } else {
            this.lockedRecipeId = recipe.id;
            List<FluidIngredient> contents = copyFluidIngredients(left);
            for (int i = 0; i < internalInventory.length; i++) {
                if (internalInventory[i].isFluidEmpty()) continue;
                contents = internalInventory[i].handleFluidInternal(contents, simulate);
                if (contents == null) {
                    if (!simulate) cacheFluidOutput(recipe);
                    this.lockedSlot = i;
                    return contents;
                }
                contents = copyFluidIngredients(left);
            }
            this.lockedSlot = -1;
            return left;
        }
    }

    private void cacheFluidOutput(GTRecipe recipe) {
        this.returnFluidMap.clear();
        for (FluidStack stack : MORecipeHelper.getOutputFluid(recipe)) {
            var key = AEFluidKey.of(stack.getFluid(), stack.getTag());
            returnFluidMap.mergeLong(key, stack.getAmount(), Long::sum);
        }
    }

    private List<Ingredient> copyIngredients(List<Ingredient> ingredients) {
        List<Ingredient> result = new ObjectArrayList<>(ingredients.size());
        for (Ingredient ingredient : ingredients) {
            result.add(ItemRecipeCapability.CAP.copyInner(ingredient));
        }
        return result;
    }

    private List<FluidIngredient> copyFluidIngredients(List<FluidIngredient> ingredients) {
        List<FluidIngredient> result = new ObjectArrayList<>(ingredients.size());
        for (FluidIngredient ingredient : ingredients) {
            result.add(FluidRecipeCapability.CAP.copyInner(ingredient));
        }
        return result;
    }

    private void refundAll(ClickData clickData) {
        if (!clickData.isCtrlClick) {
            for (InternalSlot internalSlot : internalInventory) {
                internalSlot.refund();
            }
        }
    }

    //////////////////////////////////////
    //**********     GUI     ***********//
    //////////////////////////////////////

    @Override
    public Widget createUIWidget() {
        int rowSize = 9;
        int colSize = 6;
        var group = new WidgetGroup(0, 0, 18 * rowSize + 16, 18 * colSize + 16);
        int index = 0;
        for (int y = 0; y < colSize; ++y) {
            for (int x = 0; x < rowSize; ++x) {
                int finalI = index;
                var slot = new SlotWidget(patternInventory, index++, 8 + x * 18, 14 + y * 18)
                        .setItemHook(stack -> {
                            if (!stack.isEmpty() && stack.getItem() instanceof EncodedPatternItem iep) {
                                final ItemStack out = iep.getOutput(stack);
                                if (!out.isEmpty()) {
                                    return out;
                                }
                            }
                            return stack;
                        })
                        .setChangeListener(() -> onPatternChange(finalI))
                        .setBackground(GuiTextures.SLOT, GEGuiTextures.PATTERN_OVERLAY);
                group.addWidget(slot);
            }
        }
        // ME Network status
        group.addWidget(new LabelWidget(8, 2, () -> this.isOnline ?
                "gtceu.gui.me_network.online" :
                "gtceu.gui.me_network.offline"));

        return group;
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new ButtonConfigurator(new GuiTextureGroup(GuiTextures.BUTTON, GEGuiTextures.REFUND_OVERLAY), this::refundAll)
                .setTooltips(List.of(Component.translatable("gui.gregiceng.refund_all.desc"))));
        configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(circuitInventory.storage));
        configuratorPanel.attachConfigurators(new InventoryFancyConfigurator(shareInventory.storage, Component.translatable("gui.gregiceng.share_inventory.title"))
                .setTooltips(List.of(Component.translatable("gui.gregiceng.share_inventory.desc"))));
        configuratorPanel.attachConfigurators(new TankFancyConfigurator(shareTank.getStorages(), Component.translatable("gui.gregiceng.share_tank.title"))
                .setTooltips(List.of(Component.translatable("gui.gregiceng.share_tank.desc"))));
    }

    private void onPatternChange(int index) {
        if (isRemote()) return;

        // remove old if applicable
        var internalInv = internalInventory[index];
        var newPattern = patternInventory.getStackInSlot(index);
        var newPatternDetails = PatternDetailsHelper.decodePattern(newPattern, getLevel());
        var oldPatternDetails = patternDetailsPatternSlotMap.inverse().get(internalInv);
        patternDetailsPatternSlotMap.forcePut(newPatternDetails, internalInv);
        if (oldPatternDetails != null && !oldPatternDetails.equals(newPatternDetails)) {
            internalInv.refund();
        }

        needPatternSync = true;
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return patternDetailsPatternSlotMap.keySet().stream()
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!getMainNode().isActive()
                || !patternDetailsPatternSlotMap.containsKey(patternDetails)
                || !checkInput(inputHolder)) {
            return false;
        }

        var slot = patternDetailsPatternSlotMap.get(patternDetails);
        if (slot != null) {
            slot.pushPattern(patternDetails, inputHolder);
            listeners.forEach(Runnable::run);
            return true;
        }
        return false;
    }

    private boolean checkInput(KeyCounter[] inputHolder) {
        for (KeyCounter input : inputHolder) {
            var illegal = input.keySet().stream()
                    .map(AEKey::getType)
                    .map(AEKeyType::getId)
                    .anyMatch(id -> !id.equals(AEKeyType.items().getId()) && !id.equals(AEKeyType.fluids().getId()));
            if (illegal) return false;
        }
        return true;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public void onChanged() {
        super.onChanged();
        this.updateSubscription();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public List<IRecipeHandlerTrait> getRecipeHandlers() {
        var handlers = new ArrayList<>(super.getRecipeHandlers());
        handlers.add(new ItemInputHandler());
        handlers.add(new FluidInputHandler());
        handlers.add(new ItemOutputHandler());
        handlers.add(new FluidOutputHandler());
        return handlers;
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);

    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        if (!forDrop) {

        }
    }

    protected class InternalSlot implements ITagSerializable<CompoundTag>, IContentChangeAware {

        @Getter
        @Setter
        protected Runnable onContentsChanged = () -> {/**/};

        private final Set<ItemStack> itemInventory;
        private final Set<FluidStack> fluidInventory;

        public InternalSlot() {
            this.itemInventory = new HashSet<>();
            this.fluidInventory = new HashSet<>();
        }

        public boolean isItemEmpty() {
            return itemInventory.isEmpty();
        }

        public boolean isFluidEmpty() {
            return fluidInventory.isEmpty();
        }

        private void addItem(AEItemKey key, long amount) {
            if (amount <= 0L) return;
            for (ItemStack item : itemInventory) {
                if (key.matches(item)) {
                    long sum = item.getCount() + amount;
                    if (sum <= Integer.MAX_VALUE) {
                        item.grow((int) amount);
                    } else {
                        itemInventory.remove(item);
                        itemInventory.addAll(List.of(AEUtils.toItemStacks(key, sum)));
                    }
                    return;
                }
            }
            itemInventory.addAll(List.of(AEUtils.toItemStacks(key, amount)));
        }

        private void addFluid(AEFluidKey key, long amount) {
            if (amount <= 0L) return;
            for (FluidStack fluid : fluidInventory) {
                if (AEUtils.matches(key, fluid)) {
                    long free = Long.MAX_VALUE - fluid.getAmount();
                    if (amount <= free) {
                        fluid.grow(amount);
                    } else {
                        fluid.setAmount(Long.MAX_VALUE);
                        fluidInventory.add(AEUtils.toFluidStack(key, amount - free));
                    }
                    return;
                }
            }
            fluidInventory.add(AEUtils.toFluidStack(key, amount));
        }

        public ItemStack[] getItemInputs() {
            return ArrayUtils.addAll(itemInventory.toArray(new ItemStack[0]));
        }

        public FluidStack[] getFluidInputs() {
            return fluidInventory.toArray(new FluidStack[0]);
        }

        public void refund() {
            var network = getMainNode().getGrid();
            if (network != null) {
                MEStorage networkInv = network.getStorageService().getInventory();
                var energy = network.getEnergyService();
                for (ItemStack stack : itemInventory) {
                    if (stack == null) continue;

                    var key = AEItemKey.of(stack);
                    if (key == null) continue;

                    long inserted = StorageHelper.poweredInsert(
                            energy, networkInv,
                            key, stack.getCount(),
                            actionSource);
                    if (inserted > 0) {
                        stack.shrink((int) inserted);
                        if (stack.isEmpty()) {
                            itemInventory.remove(stack);
                        }
                    }
                }

                for (FluidStack stack : fluidInventory) {
                    if (stack == null || stack.isEmpty()) continue;

                    long inserted = StorageHelper.poweredInsert(
                            energy, networkInv,
                            AEFluidKey.of(stack.getFluid(), stack.getTag()),
                            stack.getAmount(), actionSource);
                    if (inserted > 0) {
                        stack.shrink(inserted);
                        if (stack.isEmpty()) {
                            fluidInventory.remove(stack);
                        }
                    }
                }
                onContentsChanged.run();
            }
        }

        public void pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
            patternDetails.pushInputsToExternalInventory(inputHolder, (what, amount) -> {
                if (what instanceof AEFluidKey key) {
                    addFluid(key, amount);
                }

                if (what instanceof AEItemKey key) {
                    addItem(key, amount);
                }
            });
            onContentsChanged.run();
        }

        List<Ingredient> handleItemInternal(List<Ingredient> left, boolean simulate) {
            Iterator<Ingredient> iterator = left.iterator();
            while (iterator.hasNext()) {
                Ingredient ingredient = iterator.next();
                SLOT_LOOKUP:
                for (ItemStack stack : itemInventory) { // TODO 改变循环的的次序，这在大数量检测时是有用的
                    if (ingredient.test(stack)) {
                        ItemStack[] ingredientStacks = ingredient.getItems();
                        for (ItemStack ingredientStack : ingredientStacks) {
                            if (ingredientStack.is(stack.getItem())) {
                                int extracted = Math.min(ingredientStack.getCount(), stack.getCount());
                                if (!simulate) {
                                    stack.shrink(extracted);
                                    if (stack.isEmpty()) {
                                        itemInventory.remove(stack);
                                    }
                                    onContentsChanged.run();
                                }
                                ingredientStack.shrink(extracted);
                                if (ingredientStack.isEmpty()) {
                                    iterator.remove();
                                    break SLOT_LOOKUP;
                                }
                            }
                        }
                    }
                }
            }
            return left.isEmpty() ? null : left;
        }
        // TODO 是否要提前结束循环

        List<FluidIngredient> handleFluidInternal(List<FluidIngredient> left, boolean simulate) {
            Iterator<FluidIngredient> iterator = left.iterator();
            while (iterator.hasNext()) {
                FluidIngredient fluidStack = iterator.next();
                if (fluidStack.isEmpty()) {
                    iterator.remove();
                    continue;
                }
                boolean found = false;
                FluidStack foundStack = null;
                for (FluidStack stack : fluidInventory) {
                    if (!fluidStack.test(stack)) {
                        continue;
                    }
                    found = true;
                    foundStack = stack;
                }
                if (!found) continue;
                long drained = Math.min(foundStack.getAmount(), fluidStack.getAmount());
                if (!simulate) {
                    foundStack.shrink(drained);
                    if (foundStack.isEmpty()) {
                        fluidInventory.remove(foundStack);
                    }
                    onContentsChanged.run();
                }

                fluidStack.setAmount(fluidStack.getAmount() - drained);
                if (fluidStack.getAmount() <= 0) {
                    iterator.remove();
                }
            }
            return left.isEmpty() ? null : left;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();

            ListTag itemInventoryTag = new ListTag();
            for (ItemStack itemStack : this.itemInventory) {
                itemInventoryTag.add(MONBTUtils.writeItemStack(itemStack, new CompoundTag()));
            }
            tag.put("inventory", itemInventoryTag);

            ListTag fluidInventoryTag = new ListTag();
            for (FluidStack fluidStack : fluidInventory) {
                fluidInventoryTag.add(fluidStack.saveToTag(new CompoundTag()));
            }
            tag.put("fluidInventory", fluidInventoryTag);

            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            ListTag inv = tag.getList("inventory", Tag.TAG_COMPOUND);
            for (int i = 0; i < inv.size(); i++) {
                CompoundTag tagItemStack = inv.getCompound(i);
                var item = MONBTUtils.readItemStack(tagItemStack);
                if (item != null) {
                    if (!item.isEmpty()) {
                        itemInventory.add(item);
                    }
                } else {
                    GregicEng.logger().warn("An error occurred while loading contents of ME Crafting Input Bus. This item has been voided: " + tagItemStack);
                }
            }
            ListTag fluidInv = tag.getList("fluidInventory", Tag.TAG_COMPOUND);
            for (int i = 0; i < fluidInv.size(); i++) {
                CompoundTag tagFluidStack = fluidInv.getCompound(i);
                var fluid = FluidStack.loadFromTag(tagFluidStack);
                if (fluid != null) {
                    if (!fluid.isEmpty()) {
                        fluidInventory.add(fluid);
                    }
                } else {
                    GregicEng.logger().warn("An error occurred while loading contents of ME Crafting Input Bus. This fluid has been voided: " + tagFluidStack);
                }
            }
        }
    }

    //////////////////////////////////////
    //*******   Recipe Handlers  *******//
    //////////////////////////////////////

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
            circuitInventory.handleRecipeInner(io, recipe, left, slotName, simulate);
            shareInventory.handleRecipeInner(io, recipe, left, slotName, simulate);
            left = handleItemInner(recipe, left, simulate);
            return left;
        }

        @Override
        public List<Object> getContents() {
            return Arrays.stream(internalInventory)
                    .map(InternalSlot::getItemInputs)
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toList());
        }

        @Override
        public double getTotalContentAmount() {
            return Arrays.stream(internalInventory)
                    .map(InternalSlot::getItemInputs)
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
            return handleFluidInner(recipe, left, simulate);
        }

        @Override
        public List<Object> getContents() {
            return Arrays.stream(internalInventory)
                    .map(InternalSlot::getFluidInputs)
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toList());
        }

        @Override
        public double getTotalContentAmount() {
            return Arrays.stream(internalInventory)
                    .map(InternalSlot::getFluidInputs)
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
            Iterator<Ingredient> iterator = left.iterator();
            while (iterator.hasNext()) {
                Ingredient ingredient = iterator.next();
                ItemStack[] ingredientStacks = ingredient.getItems();
                SLOT_LOOKUP:
                for (ItemStack ingredientStack : ingredientStacks) {
                    for (var entry : returnItemMap.object2LongEntrySet()) {
                        var key = entry.getKey();
                        long amount = entry.getLongValue();
                        int count = ingredientStack.getCount();
                        if (key.matches(ingredientStack) && count == amount) {
                            if (!simulate) {
                                returnBuffer.mergeLong(key, amount, Long::sum);
                                returnItemMap.removeLong(key);
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
            Iterator<FluidIngredient> iterator = left.iterator();
            while (iterator.hasNext()) {
                FluidIngredient ingredient = iterator.next();
                FluidStack[] ingredientStacks = ingredient.getStacks();
                SLOT_LOOKUP:
                for (FluidStack ingredientStack : ingredientStacks) {
                    for (var entry : returnFluidMap.object2LongEntrySet()) {
                        var key = entry.getKey();
                        long amount = entry.getLongValue();
                        long count = ingredientStack.getAmount();
                        if (AEUtils.matches(key, ingredientStack) && count == amount) {
                            if (!simulate) {
                                returnBuffer.mergeLong(key, amount, Long::sum);
                                returnFluidMap.removeLong(key);
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
    }
}
