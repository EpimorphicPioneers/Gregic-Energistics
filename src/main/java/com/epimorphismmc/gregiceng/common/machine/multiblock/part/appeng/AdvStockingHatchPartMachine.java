package com.epimorphismmc.gregiceng.common.machine.multiblock.part.appeng;

import com.epimorphismmc.gregiceng.api.gui.wight.ConfigSlotWidget;
import com.epimorphismmc.gregiceng.api.machine.feature.multiblock.IAutoPullPart;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AdvStockingHatchPartMachine extends StockingHatchPartMachine implements IAutoPullPart {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            AdvStockingHatchPartMachine.class, StockingHatchPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @Getter
    @Setter
    private long minPullAmount;

    private Predicate<AEFluidKey> autoPullTest;

    public AdvStockingHatchPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, GTValues.IV, IO.IN, args);
        this.setWorkingEnabled(false);
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        // ensure that no other stocking bus on this multiblock is configured to hold the same item.
        // that we have in our own bus.
        this.autoPullTest = key -> !this.testConfiguredInOtherPart(key);
    }

    @Override
    public void removedFromController(IMultiController controller) {
        // block auto-pull from working when not in a formed multiblock
        this.autoPullTest = key -> false;
        if (isWorkingEnabled()) {
            // may as well clear if we are auto-pull, no reason to preserve the config
            this.tanks.clearConfig();
        }
        super.removedFromController(controller);
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
        if (!isRemote()) {
            if (!isWorkingEnabled()) {
                this.tanks.clearConfig();
            } else if (updateMEStatus()) {
                this.refreshList();
            }
        }
    }

    @Override
    public void update() {
        super.update();
        if (!isRemote() && isWorkingEnabled() && getOffsetTimer() % 100 == 0) {
            refreshList();
        }
    }

    /**
     * Refresh the configuration list in auto-pull mode.
     * Sets the config to the first 16 valid items found in the network.
     */
    private void refreshList() {
        this.tanks.clearConfig();
        var counter = getMainNode().getGrid().getStorageService().getInventory().getAvailableStacks();
        int index = 0;
        for (Object2LongMap.Entry<AEKey> entry : counter) {
            if (index >= CONFIG_SIZE) break;
            AEKey what = entry.getKey();
            long amount = entry.getLongValue();

            if (amount < minPullAmount) continue;
            if (!(what instanceof AEFluidKey fluidKey)) continue;
            if (autoPullTest != null && autoPullTest.test(fluidKey)) continue;

            if (autoPullTest != null && !autoPullTest.test(fluidKey)) continue;
            var slot = this.tanks.getAESlot(index);
            slot.setConfig(fluidKey);
            index++;
        }
    }

    @Override
    public ConfigSlotWidget<AEFluidKey> createConfigSlot(int index, int x, int y) {
        return super.createConfigSlot(index, x, y).setIsBlocked(this::isWorkingEnabled);
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        IAutoPullPart.super.attachConfigurators(configuratorPanel);
        super.attachConfigurators(configuratorPanel);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
