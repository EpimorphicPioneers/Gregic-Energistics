package com.epimorphismmc.gregiceng.data;

import com.epimorphismmc.monomorphism.datagen.lang.MOLangProvider;

import java.util.List;

import static com.epimorphismmc.gregiceng.common.data.GEMachines.*;

public class GELangHandler {

    private GELangHandler() {/**/}

    public static void init(MOLangProvider provider) {
        provider.addBlockWithTooltip(CRAFTING_INPUT_BUFFER::getBlock,
                "ME Crafting Input Buffer",
                "ME样板输入总成",
                List.of(

                ),
                List.of(

                ));
        provider.add("gui.gregiceng.share_inventory.title",
                "Share Inventory",
                "共享库存");
        provider.add("gui.gregiceng.share_inventory.desc",
                "Open share inventory",
                "打开共享库存");

        provider.add("gui.gregiceng.share_tank.title",
                "Share Tank",
                "共享储罐");
        provider.add("gui.gregiceng.share_tank.desc",
                "Open share tank",
                "打开共享储罐");

        provider.add("gui.gregiceng.refund_all.desc",
                "Refund raw materials in full",
                "退回所有材料");
    }
}
