package com.epimorphismmc.gregiceng.common.data;

import com.epimorphismmc.gregiceng.GregicEng;

import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;

import net.minecraft.world.item.CreativeModeTab;

import com.tterrag.registrate.util.entry.RegistryEntry;

import static com.epimorphismmc.gregiceng.GregicEng.registrate;

public class GECreativeModeTabs {
    public static final RegistryEntry<CreativeModeTab> MAIN = registrate()
            .defaultCreativeTab("main", builder -> builder
                    .displayItems(
                            new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("main", registrate()))
                    .title(registrate().addLang("itemGroup", GregicEng.id("main"), GregicEng.NAME))
                    .icon(GEMachines.CRAFTING_IO_BUFFER::asStack)
                    .build())
            .register();

    private GECreativeModeTabs() {
        /**/
    }

    public static void init() {
        /**/
    }
}
