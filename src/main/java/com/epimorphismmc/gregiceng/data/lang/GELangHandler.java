package com.epimorphismmc.gregiceng.data.lang;

import com.epimorphismmc.monomorphism.datagen.lang.MOLangProvider;

import java.util.List;

import static com.epimorphismmc.gregiceng.common.data.GEMachines.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.MULTI_HATCH_TIERS;

public class GELangHandler {

    private GELangHandler() {/**/}

    public static void init(MOLangProvider provider) {
        provider.addBlockWithTooltip(CRAFTING_IO_BUFFER::getBlock,
            "ME Crafting IO Buffer",
            "ME样板IO总成",
            List.of(

            ),
            List.of(

            ));

        provider.addBlockWithTooltip(STOCKING_BUS::getBlock,
            "ME Stocking Input Bus",
            "ME存储输入总线",
            List.of(

            ),
            List.of(

            ));

        provider.addBlockWithTooltip(STOCKING_HATCH::getBlock,
            "ME Stocking Input Hatch",
            "ME存储输入仓",
            List.of(

            ),
            List.of(

            ));

        provider.addBlockWithTooltip(ADV_STOCKING_BUS::getBlock,
            "ME Advanced Stocking Input Bus",
            "ME进阶存储输入总线",
            List.of(

            ),
            List.of(

            ));

        provider.addBlockWithTooltip(ADV_STOCKING_HATCH::getBlock,
            "ME Advanced Stocking Input Hatch",
            "ME进阶存储输入仓",
            List.of(

            ),
            List.of(

            ));

        provider.addTieredMachineName("input_buffer", "输入总成", MULTI_HATCH_TIERS);
        provider.addBlockWithTooltip("input_buffer",
            "Item and Fluid Input for Multiblocks",
            "为多方块结构输入物品和流体");

        provider.addTieredMachineName("output_buffer", "输出总成", MULTI_HATCH_TIERS);
        provider.addBlockWithTooltip("output_buffer",
            "Item and Fluid Output for Multiblocks",
            "为多方块结构输出物品和流体");

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

        provider.add("gui.gregiceng.automatic_return.desc.enabled",
            "Automatic Return is on",
            "自动回流已开启");
        provider.add("gui.gregiceng.automatic_return.desc.disabled",
            "Automatic Return is disabled",
            "自动回流已禁用");

        provider.add("gui.gregiceng.auto_pull_me.desc.enabled",
            "Automatic Return is on",
            "自动回流已开启");
        provider.add("gui.gregiceng.auto_pull_me.desc.disabled",
            "Automatic Return is disabled",
            "自动回流已禁用");

        provider.add("gui.gregiceng.rename.desc",
            "Rename",
            "重命名");

        provider.add("config.jade.plugin_gregiceng.crafting_io_buffer",
            "Crafting IO Buffer",
            "样板IO总成"
        );

        provider.add("config.gregiceng.option.enableMoreAbility",
            "Enable More Ability for Crafting IO Buffer/Slave",
            "为样板IO总成/镜像启用更多能力")
        ;
    }
}
