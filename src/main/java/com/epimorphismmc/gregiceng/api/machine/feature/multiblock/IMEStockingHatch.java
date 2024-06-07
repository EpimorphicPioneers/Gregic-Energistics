package com.epimorphismmc.gregiceng.api.machine.feature.multiblock;

import appeng.api.stacks.AEFluidKey;
import com.epimorphismmc.gregiceng.api.gui.wight.AESlotWidget;
import com.epimorphismmc.gregiceng.api.gui.wight.ConfigSlotWidget;
import com.epimorphismmc.gregiceng.api.gui.wight.FluidAESlotWidget;
import com.epimorphismmc.gregiceng.api.gui.wight.FluidConfigSlotWidget;

public interface IMEStockingHatch extends IMEStockingPart<AEFluidKey> {
    @Override
    default AESlotWidget<AEFluidKey> createAESlot(int index, int x, int y) {
        return new FluidAESlotWidget(getAESlotList().getAESlot(index), x, y);
    }

    @Override
    default ConfigSlotWidget<AEFluidKey> createConfigSlot(int index, int x, int y) {
        return new FluidConfigSlotWidget(getAESlotList(), index, x, y, this::testConfiguredInOtherPart);
    }
}
