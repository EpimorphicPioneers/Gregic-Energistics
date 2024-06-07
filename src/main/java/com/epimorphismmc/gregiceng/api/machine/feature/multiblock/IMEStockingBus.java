package com.epimorphismmc.gregiceng.api.machine.feature.multiblock;

import appeng.api.stacks.AEItemKey;
import com.epimorphismmc.gregiceng.api.gui.wight.AESlotWidget;
import com.epimorphismmc.gregiceng.api.gui.wight.ConfigSlotWidget;
import com.epimorphismmc.gregiceng.api.gui.wight.ItemAESlotWidget;
import com.epimorphismmc.gregiceng.api.gui.wight.ItemConfigSlotWidget;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart;

public interface IMEStockingBus extends IMEStockingPart<AEItemKey>, IDistinctPart {

    @Override
    default AESlotWidget<AEItemKey> createAESlot(int index, int x, int y) {
        return new ItemAESlotWidget(getAESlotList().getAESlot(index), x, y);
    }

    @Override
    default ConfigSlotWidget<AEItemKey> createConfigSlot(int index, int x, int y) {
        return new ItemConfigSlotWidget(getAESlotList(), index, x, y, this::testConfiguredInOtherPart);
    }
}
