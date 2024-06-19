package com.epimorphismmc.gregiceng.common.machine.multiblock.part.appeng;

import com.epimorphismmc.gregiceng.api.machine.feature.multiblock.IMEStockingBus;
import com.epimorphismmc.gregiceng.api.misc.ConfigurableAESlot;
import com.epimorphismmc.gregiceng.api.misc.IConfigurableAESlotList;

import com.epimorphismmc.monomorphism.ae2.MEPartMachine;
import com.epimorphismmc.monomorphism.machine.fancyconfigurator.InventoryFancyConfigurator;
import com.epimorphismmc.monomorphism.transfer.item.BigItemStackTransfer;

import appeng.api.networking.IStackWatcher;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageWatcherNode;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.me.ManagedGridNode;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.ItemHandlerProxyRecipeTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;

import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import com.google.common.primitives.Ints;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler.handleIngredient;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StockingBusPartMachine extends MEPartMachine implements IMEStockingBus {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER =
            new ManagedFieldHolder(StockingBusPartMachine.class, MEPartMachine.MANAGED_FIELD_HOLDER);

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

    public StockingBusPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, GTValues.EV, IO.IN, args);
        this.inventory = new ExportOnlyAEItemList(this, 5 * 5);
        this.circuitInventory = new NotifiableItemStackHandler(this, 1, IO.IN, IO.NONE)
                .setFilter(IntCircuitBehaviour::isIntegratedCircuit);
        this.shareInventory = new NotifiableItemStackHandler(this, 9, IO.IN, IO.NONE);
        this.combinedInventory = new ItemHandlerProxyRecipeTrait(
                this, Set.of(inventory, circuitInventory, shareInventory), IO.IN, IO.NONE);
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(1, this::validateConfig));
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        combinedInventory.recomputeEnabledState();
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

    //////////////////////////////////////
    // **********     GUI     ***********//
    //////////////////////////////////////

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                        GuiTextures.BUTTON_DISTINCT_BUSES.getSubTexture(0, 0.5, 1, 0.5),
                        GuiTextures.BUTTON_DISTINCT_BUSES.getSubTexture(0, 0, 1, 0.5),
                        this::isDistinct,
                        (clickData, pressed) -> setDistinct(pressed))
                .setTooltipsSupplier(
                        pressed -> List.of(Component.translatable("gtceu.multiblock.universal.distinct")
                                .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW))
                                .append(Component.translatable(
                                        pressed
                                                ? "gtceu.multiblock.universal.distinct.yes"
                                                : "gtceu.multiblock.universal.distinct.no")))));
        configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(circuitInventory.storage));
        configuratorPanel.attachConfigurators(new InventoryFancyConfigurator(
                        shareInventory.storage, Component.translatable("gui.gregiceng.share_inventory.title"))
                .setTooltips(List.of(Component.translatable("gui.gregiceng.share_inventory.desc"))));
    }

    @Override
    public boolean testConfiguredInOtherPart(@Nullable AEItemKey config) {
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

    protected class ExportOnlyAEItemList extends NotifiableRecipeHandlerTrait<Ingredient>
            implements IConfigurableAESlotList<AEItemKey> {
        public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
                ExportOnlyAEItemList.class, NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);

        @Persisted
        ExportOnlyAEItem[] inventory;

        private ItemStackTransfer itemTransfer;

        private IStackWatcher storageWatcher;

        public ExportOnlyAEItemList(MetaMachine holder, int slots) {
            super(holder);
            this.inventory = new ExportOnlyAEItem[slots];
            for (int i = 0; i < slots; i++) {
                inventory[i] = new ExportOnlyAEItem(null);
                inventory[i].setOnContentsChanged(this::onChanged);
            }

            IStorageWatcherNode stackWatcherNode = new IStorageWatcherNode() {
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
            getMainNode().addService(IStorageWatcherNode.class, stackWatcherNode);
        }

        private void configureWatchers() {
            if (storageWatcher != null) {
                storageWatcher.reset();
                for (AEItemKey aeItemKey : getAEKeySet()) {
                    storageWatcher.add(aeItemKey);
                }
            }
        }

        @Override
        public void onChanged() {
            super.onChanged();
            notifyListeners();
            configureWatchers();
        }

        @Override
        public ExportOnlyAEItem getAESlot(int index) {
            return inventory[index];
        }

        @Override
        public int getSlots() {
            return inventory.length;
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

        public ItemStackTransfer getTransfer() {
            if (this.itemTransfer == null) {
                this.itemTransfer = new WrappedItemStackTransfer(inventory);
            }
            return itemTransfer;
        }

        @Override
        public @Nullable List<Ingredient> handleRecipeInner(
                IO io,
                GTRecipe recipe,
                List<Ingredient> left,
                @Nullable String slotName,
                boolean simulate) {
            return handleIngredient(io, recipe, left, simulate, getHandlerIO(), getTransfer());
        }

        @Override
        public List<Object> getContents() {
            return Arrays.stream(inventory)
                    .map(ExportOnlyAEItem::getStack)
                    .filter(stack -> !stack.isEmpty())
                    .collect(Collectors.toUnmodifiableList());
        }

        @Override
        public double getTotalContentAmount() {
            return Arrays.stream(inventory).mapToInt(ExportOnlyAEItem::getCount).sum();
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

        private static class WrappedItemStackTransfer extends ItemStackTransfer {

            private final ExportOnlyAEItem[] inventory;

            public WrappedItemStackTransfer(ExportOnlyAEItem[] inventory) {
                super();
                this.inventory = inventory;
            }

            @Override
            public int getSlots() {
                return inventory.length;
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                return inventory[slot].getStack();
            }

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                // NO-OP
            }

            @Override
            public ItemStack insertItem(
                    int slot, ItemStack stack, boolean simulate, boolean notifyChanges) {
                return stack;
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
                if (amount == 0) return ItemStack.EMPTY;
                validateSlotIndex(slot);
                return inventory[slot].extract(amount, simulate);
            }

            @Override
            protected void validateSlotIndex(int slot) {
                if (slot < 0 || slot >= getSlots())
                    throw new RuntimeException(
                            "Slot " + slot + " not in valid range - [0," + getSlots() + ")");
            }

            @Override
            public int getSlotLimit(int slot) {
                return Integer.MAX_VALUE;
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return false;
            }

            @Override
            public ItemStackTransfer copy() {
                var copy = new BigItemStackTransfer(getSlots(), true, Integer.MAX_VALUE);
                for (int i = 0; i < inventory.length; i++) {
                    copy.setStackInSlot(i, getStackInSlot(i));
                }
                return copy;
            }
        }
    }

    protected class ExportOnlyAEItem extends ConfigurableAESlot<AEItemKey> {

        public ExportOnlyAEItem(@Nullable AEItemKey config) {
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

        public ItemStack extract(int amount, boolean simulate) {
            int extracted = Ints.saturatedCast(request(amount, simulate));
            if (config != null && extracted > 0) {
                return config.toStack(extracted);
            }
            return ItemStack.EMPTY;
        }

        public ItemStack getStack() {
            int count = Ints.saturatedCast(getAmount());
            if (config != null && count > 0) {
                return config.toStack(count);
            }
            return ItemStack.EMPTY;
        }

        public int getCount() {
            return Ints.saturatedCast(getAmount());
        }

        public void onContentsChanged() {
            if (onContentsChanged != null) {
                onContentsChanged.run();
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
