package com.epimorphismmc.gregiceng.common.machine.multiblock.part.appeng;

import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IStackWatcher;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageWatcherNode;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.me.ManagedGridNode;
import com.epimorphismmc.gregiceng.api.machine.feature.multiblock.IMEStockingBus;
import com.epimorphismmc.gregiceng.api.misc.ConfigurableAESlot;
import com.epimorphismmc.gregiceng.api.misc.IConfigurableAESlotList;
import com.epimorphismmc.gregiceng.api.misc.SerializableItemTransferList;
import com.epimorphismmc.monomorphism.ae2.MEPartMachine;
import com.epimorphismmc.monomorphism.machine.fancyconfigurator.InventoryFancyConfigurator;
import com.google.common.primitives.Ints;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.ItemHandlerProxyRecipeTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.misc.ItemTransferList;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler.handleIngredient;

public class StockingBusPartMachine extends MEPartMachine implements IMEStockingBus {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(StockingBusPartMachine.class, MEPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    protected final ExportOnlyAEItemList inventory;
    @Getter
    @Persisted
    protected final NotifiableItemStackHandler shareInventory;
    @Getter
    @Persisted
    protected final NotifiableItemStackHandler circuitInventory;
    @Getter
    protected final ItemHandlerProxyRecipeTrait combinedInventory;

    @Nullable
    protected TickableSubscription updateSubs;

    public StockingBusPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, GTValues.EV, IO.IN, args);
        this.inventory = new ExportOnlyAEItemList(this, 5 * 5);
        this.circuitInventory = new NotifiableItemStackHandler(this, 1, IO.IN, IO.NONE).setFilter(IntCircuitBehaviour::isIntegratedCircuit);
        this.shareInventory = new NotifiableItemStackHandler(this, 9, IO.IN, IO.NONE);
        this.combinedInventory = new ItemHandlerProxyRecipeTrait(this, Set.of(inventory, circuitInventory, shareInventory), IO.IN, IO.NONE);
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(1, this::validateConfig));
        }
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        this.updateSubscription();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        combinedInventory.recomputeEnabledState();
    }

    protected void updateSubscription() {
        if (getMainNode().isOnline()) {
            updateSubs = subscribeServerTick(updateSubs, this::update);
        } else if (updateSubs != null) {
            updateSubs.unsubscribe();
            updateSubs = null;
        }
    }

    protected void update() {

    }

    @Override
    public boolean isDistinct() {
        return combinedInventory.isDistinct();
    }

    @Override
    public void setDistinct(boolean isDistinct) {
        combinedInventory.setDistinct(isDistinct);
        if (!isRemote() && !isDistinct) {
            // Ensure that our configured items won't match any other buses in the multiblock.
            // Needed since we allow duplicates in distinct mode on, but not off
            validateConfig();
        }
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
            GuiTextures.BUTTON_DISTINCT_BUSES.getSubTexture(0, 0.5, 1, 0.5),
            GuiTextures.BUTTON_DISTINCT_BUSES.getSubTexture(0, 0, 1, 0.5),
            this::isDistinct, (clickData, pressed) -> setDistinct(pressed))
            .setTooltipsSupplier(pressed -> List.of(
                Component.translatable("gtceu.multiblock.universal.distinct").setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW))
                    .append(Component.translatable(pressed ? "gtceu.multiblock.universal.distinct.yes" : "gtceu.multiblock.universal.distinct.no")))));
        configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(circuitInventory.storage));
        configuratorPanel.attachConfigurators(new InventoryFancyConfigurator(shareInventory.storage, Component.translatable("gui.gregiceng.share_inventory.title"))
            .setTooltips(List.of(Component.translatable("gui.gregiceng.share_inventory.desc"))));
    }

    @Override
    public boolean testConfiguredInOtherPart(@NotNull AEItemKey config) {
        if (!isFormed() || isDistinct()) return true;

        for (IMultiController controller : getControllers()) {
            for (IMultiPart part : controller.getParts()) {
                if (part instanceof IMEStockingBus bus) {
                    if (bus == this || bus.isDistinct()) continue;
                    if (bus.getAESlotList().hasConfig(config)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public IConfigurableAESlotList<AEItemKey> getAESlotList() {
        return inventory;
    }

    @Override
    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected class ExportOnlyAEItemList extends NotifiableRecipeHandlerTrait<Ingredient> implements IConfigurableAESlotList<AEItemKey> {
        public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ExportOnlyAEItemList.class, NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);

        @Persisted
        ItemTransferList inventory;

        private IStackWatcher storageWatcher;
        private final IStorageWatcherNode stackWatcherNode = new IStorageWatcherNode() {
            @Override
            public void updateWatcher(IStackWatcher newWatcher) {
                storageWatcher = newWatcher;
                configureWatchers();
            }

            @Override
            public void onStackChange(AEKey what, long amount) {
                notifyListeners();
            }
        };

        public ExportOnlyAEItemList(MetaMachine holder, int slots) {
            super(holder);
            var transfers = new ExportOnlyAEItem[slots];
            for (int i = 0; i < slots; i++) {
                transfers[i] = new ExportOnlyAEItem(null);
                transfers[i].setOnContentsChanged(this::onChanged);
            }
            this.inventory = new SerializableItemTransferList(transfers);

            getMainNode().addService(IStorageWatcherNode.class, stackWatcherNode);
        }

        @Override
        public void onChanged() {
            super.onChanged();
            notifyListeners();
            configureWatchers();
        }

        @Override
        public ExportOnlyAEItem getAESlot(int index) {
            return (ExportOnlyAEItem) inventory.transfers[index];
        }

        @Override
        public int getSlots() {
            return inventory.getSlots();
        }

        @Override
        public boolean hasConfig(@Nullable AEItemKey key) {
            for (int i = 0; i < getSlots(); i++) {
                var other = getAESlot(i).getConfig();
                if (other != null && other.equals(key)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void clearConfig() {
            for (int i = 0; i < getSlots(); i++) {
                getAESlot(i).setConfig(null);
            }
        }

        @Override
        public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left,
                                                  @Nullable String slotName, boolean simulate) {
            return handleIngredient(io, recipe, left, simulate, getHandlerIO(),
                new ItemStackTransfer(NonNullList.of(ItemStack.EMPTY,
                    Arrays.stream(inventory.transfers).map(item -> item.getStackInSlot(0)).toArray(ItemStack[]::new))) {

                    @NotNull
                    @Override
                    public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
                        ItemStack extracted = super.extractItem(slot, amount, simulate, notifyChanges);
                        if (!extracted.isEmpty()) {
                            inventory.transfers[slot].extractItem(0, amount, simulate, notifyChanges);
                        }
                        return extracted;
                    }
                });
        }

        @Override
        public List<Object> getContents() {
            return Arrays.stream(inventory.transfers)
                .map(transfer -> transfer.getStackInSlot(0))
                .filter(stack -> !stack.isEmpty())
                .collect(Collectors.toUnmodifiableList());
        }

        @Override
        public double getTotalContentAmount() {
            return Arrays.stream(inventory.transfers)
                .map(transfer -> transfer.getStackInSlot(0))
                .mapToInt(ItemStack::getCount)
                .sum();
        }

        @Override
        public RecipeCapability<Ingredient> getCapability() {
            return ItemRecipeCapability.CAP;
        }

        @Override
        public IO getHandlerIO() {
            return IO.IN;
        }

        @Override
        public ManagedFieldHolder getFieldHolder() {
            return MANAGED_FIELD_HOLDER;
        }

        private void configureWatchers() {
            if (storageWatcher != null) {
                storageWatcher.reset();
                for (AEItemKey aeItemKey : getAEKeySet()) {
                    storageWatcher.add(aeItemKey);
                }
            }
        }
    }

    protected class ExportOnlyAEItem extends ConfigurableAESlot<AEItemKey> implements IItemTransfer {

        public ExportOnlyAEItem(AEItemKey config) {
            super(config);
        }

        public ExportOnlyAEItem() {
            super();
        }

        @Override
        protected IActionSource getActionSource() {
            return actionSource;
        }

        @Override
        protected ManagedGridNode getNode() {
            return getMainNode();
        }

        @Override
        public void setConfig(@Nullable AEItemKey config) {
            super.setConfig(config);
            this.onContentsChanged();
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            // NO-OP
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate, boolean notifyChanges) {
            return stack;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            int count = Ints.saturatedCast(getAmount());
            if (count > 0) {
                return config.toStack(count);
            }
            return ItemStack.EMPTY;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
            int extracted = Ints.saturatedCast(request(amount, simulate));
            if (extracted > 0) {
                return config.toStack(extracted);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }

        @Override
        public void onContentsChanged() {
            if (onContentsChanged != null) {
                onContentsChanged.run();
            }
        }

        @NotNull
        @Override
        public Object createSnapshot() {
            return config;
        }

        @Override
        public void restoreFromSnapshot(Object snapshot) {
            if (snapshot instanceof AEItemKey key) {
                this.config = key;
            }
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            if (this.config != null) {
                CompoundTag configTag = config.toTag();
                tag.put(CONFIG_TAG, configTag);
            }
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains(CONFIG_TAG)) {
                this.config = AEItemKey.fromTag(tag.getCompound(CONFIG_TAG));
            }
        }

        @Override
        public ExportOnlyAEItem copy() {
            return new ExportOnlyAEItem(config);
        }
    }
}
