package com.epimorphismmc.gregiceng.common.machine.multiblock.part.appeng;

import com.epimorphismmc.gregiceng.api.machine.feature.multiblock.IMEStockingHatch;
import com.epimorphismmc.gregiceng.api.misc.ConfigurableAESlot;
import com.epimorphismmc.gregiceng.api.misc.IConfigurableAESlotList;
import com.epimorphismmc.gregiceng.api.misc.SerializableFluidTransferList;

import com.epimorphismmc.monomorphism.ae2.AEUtils;
import com.epimorphismmc.monomorphism.ae2.MEPartMachine;

import appeng.api.networking.IStackWatcher;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageWatcherNode;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.me.ManagedGridNode;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;

import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidStorage;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank.handleIngredient;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StockingHatchPartMachine extends MEPartMachine implements IMEStockingHatch {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER =
            new ManagedFieldHolder(StockingHatchPartMachine.class, MEPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    protected final ExportOnlyAEFluidList tanks;

    public StockingHatchPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, GTValues.EV, IO.IN, args);
        this.tanks = new ExportOnlyAEFluidList(this, 5 * 5);
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(1, this::validateConfig));
        }
    }

    @Override
    public IConfigurableAESlotList<AEFluidKey> getAESlotList() {
        return tanks;
    }

    @Override
    public boolean testConfiguredInOtherPart(@Nullable AEFluidKey config) {
        if (!isFormed()) return true;

        for (IMultiController controller : getControllers()) {
            for (IMultiPart part : controller.getParts()) {
                if (part instanceof IMEStockingHatch bus) {
                    if (bus == this) continue;
                    if (bus.getAESlotList().hasConfig(config)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    //////////////////////////////////////
    // **********     GUI     ***********//
    //////////////////////////////////////

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {}

    @Override
    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected class ExportOnlyAEFluidList extends NotifiableRecipeHandlerTrait<FluidIngredient>
            implements IConfigurableAESlotList<AEFluidKey> {
        public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
                ExportOnlyAEFluidList.class, NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);

        @Persisted
        private final SerializableFluidTransferList tanks;

        private FluidStorage[] fluidStorages;

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

        public ExportOnlyAEFluidList(MetaMachine machine, int slots) {
            super(machine);
            var storages = new ExportOnlyAEFluid[slots];
            for (int i = 0; i < slots; i++) {
                storages[i] = new ExportOnlyAEFluid(null);
                storages[i].setOnContentsChanged(this::onChanged);
            }
            this.tanks = new SerializableFluidTransferList(storages);
            this.fluidStorages = null;

            getMainNode().addService(IStorageWatcherNode.class, stackWatcherNode);
        }

        @Override
        public void onChanged() {
            super.onChanged();
            notifyListeners();
            configureWatchers();
        }

        @Override
        public ExportOnlyAEFluid getAESlot(int index) {
            return (ExportOnlyAEFluid) tanks.transfers[index];
        }

        @Override
        public int getSlots() {
            return tanks.getTanks();
        }

        @Override
        public boolean hasConfig(@Nullable AEFluidKey key) {
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

        public FluidStorage[] getStorages() {
            if (this.fluidStorages == null) {
                this.fluidStorages = Arrays.stream(tanks.transfers)
                        .map(transfer ->
                                new WrappedFluidStorage(transfer.getTankCapacity(0), (ExportOnlyAEFluid) transfer))
                        .toArray(FluidStorage[]::new);
            }
            return this.fluidStorages;
        }

        @Override
        public @Nullable List<FluidIngredient> handleRecipeInner(
                IO io,
                GTRecipe recipe,
                List<FluidIngredient> left,
                @Nullable String slotName,
                boolean simulate) {
            return handleIngredient(io, recipe, left, simulate, getHandlerIO(), getStorages());
        }

        @Override
        public List<Object> getContents() {
            return Arrays.stream(tanks.transfers)
                    .map(transfer -> transfer.getFluidInTank(0))
                    .filter(stack -> !stack.isEmpty())
                    .collect(Collectors.toUnmodifiableList());
        }

        @Override
        public double getTotalContentAmount() {
            return Arrays.stream(tanks.transfers)
                    .map(transfer -> transfer.getFluidInTank(0))
                    .mapToLong(FluidStack::getAmount)
                    .sum();
        }

        @Override
        public RecipeCapability<FluidIngredient> getCapability() {
            return FluidRecipeCapability.CAP;
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
                for (AEFluidKey aeItemKey : getAEKeySet()) {
                    storageWatcher.add(aeItemKey);
                }
            }
        }

        private static class WrappedFluidStorage extends FluidStorage {

            private final ExportOnlyAEFluid fluid;

            public WrappedFluidStorage(long capacity, ExportOnlyAEFluid fluid) {
                super(capacity);
                this.fluid = fluid;
            }

            public WrappedFluidStorage(
                    long capacity, Predicate<FluidStack> validator, ExportOnlyAEFluid fluid) {
                super(capacity, validator);
                this.fluid = fluid;
            }

            @Override
            public FluidStack getFluid() {
                return this.fluid.getFluid();
            }

            @Override
            public FluidStack drain(FluidStack maxDrain, boolean simulate, boolean notifyChanges) {
                return fluid.drain(maxDrain, simulate, notifyChanges);
            }

            @Override
            public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChange) {
                return fluid.fill(tank, resource, simulate, notifyChange);
            }

            @Override
            public FluidStorage copy() {
                var storage = new WrappedFluidStorage(capacity, validator, this.fluid);
                storage.setFluid(super.fluid.copy());
                return storage;
            }
        }
    }

    protected class ExportOnlyAEFluid extends ConfigurableAESlot<AEFluidKey>
            implements IFluidStorage {

        public ExportOnlyAEFluid(@Nullable AEFluidKey config) {
            super(config);
        }

        public ExportOnlyAEFluid() {
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
        public void setConfig(@Nullable AEFluidKey config) {
            super.setConfig(config);
            this.onContentsChanged();
        }

        @Override
        public FluidStack getFluid() {
            long amount = getAmount();
            if (config != null && amount > 0) {
                return AEUtils.toFluidStack(config, amount);
            }
            return FluidStack.empty();
        }

        @Override
        public void setFluid(FluidStack fluid) {}

        @Override
        public long getFluidAmount() {
            return getAmount();
        }

        @Override
        public long getCapacity() {
            // Its capacity is always 0.
            return 0;
        }

        @Override
        public long fill(FluidStack resource, boolean doFill) {
            return 0;
        }

        @Override
        public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
            return 0;
        }

        @Override
        public boolean supportsFill(int tank) {
            return false;
        }

        @Override
        public FluidStack drain(
                int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
            return this.drain(resource, simulate, notifyChanges);
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain, boolean notifyChanges) {
            if (config != null && AEUtils.matches(config, resource)) {
                return this.drain(resource.getAmount(), doDrain, notifyChanges);
            }
            return FluidStack.empty();
        }

        @Override
        public FluidStack drain(long maxDrain, boolean simulate, boolean notifyChanges) {
            long extracted = request(maxDrain, simulate);
            if (config != null && extracted > 0) {
                return AEUtils.toFluidStack(config, extracted);
            }
            return FluidStack.empty();
        }

        @Override
        public boolean supportsDrain(int tank) {
            return tank == 0;
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return false;
        }

        @Override
        public void onContentsChanged() {
            if (onContentsChanged != null) {
                onContentsChanged.run();
            }
        }

        @Override
        public Object createSnapshot() {
            return Objects.requireNonNullElse(config, new Object());
        }

        @Override
        public void restoreFromSnapshot(Object snapshot) {
            if (snapshot instanceof AEFluidKey key) {
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
                this.config = AEFluidKey.fromTag(tag.getCompound(CONFIG_TAG));
            }
        }

        @Override
        public ExportOnlyAEFluid copy() {
            return new ExportOnlyAEFluid(config);
        }
    }
}
