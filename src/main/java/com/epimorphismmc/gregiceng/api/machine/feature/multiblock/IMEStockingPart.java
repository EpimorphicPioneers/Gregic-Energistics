package com.epimorphismmc.gregiceng.api.machine.feature.multiblock;

import appeng.api.stacks.AEKey;
import com.epimorphismmc.gregiceng.api.gui.GEGuiTextures;
import com.epimorphismmc.gregiceng.api.gui.wight.AESlotWidget;
import com.epimorphismmc.gregiceng.api.gui.wight.ConfigSlotWidget;
import com.epimorphismmc.gregiceng.api.misc.IConfigurableAESlotList;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import org.jetbrains.annotations.NotNull;

public interface IMEStockingPart<T extends AEKey> extends IFancyUIMachine {
    int CONFIG_SIZE = 5 * 5;

    @Override
    default Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 18 * 10 + 31, 18 * 5 + 16);
        var slotsContainer = new WidgetGroup(4, 12, 18 * 10 + 23, 18 * 5 + 8);
        slotsContainer.addWidget(new ImageWidget(93, 41, 18, 18, GEGuiTextures.SMALL_ARROW_OVERLAY));
        addConfigSlots(slotsContainer, 4, 4);
        addAESlots(slotsContainer, 109, 4);
        slotsContainer.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(slotsContainer);

        // ME Network status
        group.addWidget(new LabelWidget(4, 2, () -> this.isOnline()
            ? "gtceu.gui.me_network.online"
            : "gtceu.gui.me_network.offline"));
        return group;
    }

    default void addConfigSlots(WidgetGroup container, int x, int y) {
        int index = 0;
        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 5; i++) {
                container.addWidget(createConfigSlot(index++, x + i * 18, y + j * 18));
            }
        }
    }
    default void addAESlots(WidgetGroup container, int x, int y) {
        int index = 0;
        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 5; i++) {
                container.addWidget(createAESlot(index++, x + i * 18, y + j * 18));
            }
        }
    }

    AESlotWidget<T> createAESlot(int index, int x, int y);
    ConfigSlotWidget<T> createConfigSlot(int index, int x, int y);

    boolean isOnline();
    IConfigurableAESlotList<T> getAESlotList();
    boolean testConfiguredInOtherPart(@NotNull T config);

    default void validateConfig() {
        for (int i = 0; i < getAESlotList().getSlots(); i++) {
            var slot = getAESlotList().getAESlot(i);
            var config = slot.getConfig();
            if (config != null) {
                if (!testConfiguredInOtherPart(config)) {
                    slot.setConfig(null);
                }
            }
        }
    }
}
