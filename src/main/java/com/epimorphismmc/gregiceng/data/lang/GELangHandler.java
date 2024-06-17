package com.epimorphismmc.gregiceng.data.lang;

import com.epimorphismmc.monomorphism.datagen.lang.MOLangProvider;

import java.util.List;

import static com.epimorphismmc.gregiceng.common.data.GEMachines.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;

public class GELangHandler {

    private GELangHandler() {}

    public static void init(MOLangProvider provider) {
        provider.addBlockWithTooltip(
                CRAFTING_IO_BUFFER::getBlock,
                "ME Crafting IO Buffer",
                "ME样板IO总成",
                List.of("", "", ""),
                List.of("为多方块结构IO物品和流体", "可以直接处理放入的样板，每个样板拥有独立的内部存储", "拥有自动回流功能，可以将产物返回ME网络"));

        provider.addBlockWithTooltip(
                CRAFTING_IO_SLAVE::getBlock,
                "ME Crafting IO Slave",
                "ME样板IO镜像",
                List.of(""),
                List.of(
                        "无需与ME网络连接，ME样板IO总成的镜像端",
                        "拥有被复制ME样板IO总成的所有配置，包括不消耗品和处理样板",
                        "使用闪存左键复制ME样板IO总成信息",
                        "再使用闪存右键粘贴至ME样板IO镜像"));

        provider.addBlockWithTooltip(
                STOCKING_BUS::getBlock,
                "ME Stocking Input Bus",
                "ME存储输入总线",
                List.of(""),
                List.of("直接使用ME网络向多方块结构提供物品"));

        provider.addBlockWithTooltip(
                STOCKING_HATCH::getBlock,
                "ME Stocking Input Hatch",
                "ME存储输入仓",
                List.of(""),
                List.of("直接使用ME网络向多方块结构提供流体"));

        provider.addBlockWithTooltip(
                ADV_STOCKING_BUS::getBlock,
                "ME Advanced Stocking Input Bus",
                "ME进阶存储输入总线",
                List.of(""),
                List.of("直接使用ME网络向多方块结构提供物品", "拥有自动拉取功能，可以自动标记ME网络中的前25种物品", "小于最小拉取数量的物品不会被自动标记"));

        provider.addBlockWithTooltip(
                ADV_STOCKING_HATCH::getBlock,
                "ME Advanced Stocking Input Hatch",
                "ME进阶存储输入仓",
                List.of(""),
                List.of("直接使用ME网络向多方块结构提供流体", "拥有自动拉取功能，可以自动标记ME网络中的前25种流体", "小于最小拉取数量的流体不会被自动标记"));

        provider.addTieredMachineName("input_buffer", "输入总成", MULTI_HATCH_TIERS);
        provider.addBlockWithTooltip(
                "input_buffer", "Item and Fluid Input for Multiblocks", "为多方块结构输入物品和流体");

        provider.addTieredMachineName("output_buffer", "输出总成", MULTI_HATCH_TIERS);
        provider.addBlockWithTooltip(
                "output_buffer", "Item and Fluid Output for Multiblocks", "为多方块结构输出物品和流体");

        provider.add("gui.gregiceng.share_inventory.title", "Share Inventory", "共享库存");
        provider.addMultiLang(
                "gui.gregiceng.share_inventory.desc",
                List.of("Open share inventory", ""),
                List.of("打开共享库存", "其中的物品在所有内部存储之间共享"));

        provider.add("gui.gregiceng.share_tank.title", "Share Tank", "共享储罐");
        provider.addMultiLang(
                "gui.gregiceng.share_tank.desc",
                List.of("Open share tank", ""),
                List.of("打开共享储罐", "其中的流体在所有内部存储之间共享"));

        provider.add("gui.gregiceng.refund_all.desc", "Refund raw materials in full", "退回所有材料");

        provider.add("gui.gregiceng.auto_return.desc.enabled", "Automatic Return is on", "自动回流已开启");
        provider.add(
                "gui.gregiceng.auto_return.desc.disabled", "Automatic Return is disabled", "自动回流已禁用");

        provider.add("gui.gregiceng.auto_pull_me.desc.enabled", "Automatic pull is on", "自动拉取已开启");
        provider.add(
                "gui.gregiceng.auto_pull_me.desc.disabled", "Automatic pull is disabled", "自动拉取已禁用");

        provider.add("gui.gregiceng.config_disabled.desc", "", "自动拉取模式下无法进行手动设置");

        provider.add("gui.gregiceng.rename.desc", "Rename", "重命名");

        provider.add("config.jade.plugin_gregiceng.crafting_io_buffer", "Crafting IO Buffer", "样板IO总成");

        provider.add(
                "config.gregiceng.option.enableMoreAbility", "Enable More Ability for Buffer", "为总成启用更多能力");

        provider.add(
                "gregiceng.tooltip.buffer_bind",
                "Bind to X: %s, Y: %s, Z: %s Crafting IO Buffer",
                "绑定至 X: %s, Y: %s, Z: %s 样板IO总成");
    }
}
