package com.epimorphismmc.gregiceng.common;

import com.epimorphismmc.gregiceng.GregicEng;
import com.epimorphismmc.gregiceng.common.data.GECreativeModeTabs;
import com.epimorphismmc.gregiceng.common.data.GEMachines;
import com.epimorphismmc.gregiceng.common.item.GEDataStickBehavior;
import com.epimorphismmc.monomorphism.proxy.base.ICommonProxyBase;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.GTItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CommonProxy implements ICommonProxyBase {

    public CommonProxy() {
        GregicEng.logger().info("Gregic Energistics's Initialization Completed!");
    }

    @Override
    public void registerEventHandlers() {

    }

    @Override
    public void registerCapabilities() {

    }

    /* -------------------------------------------------- Registration Methods -------------------------------------------------- */

    @Override
    public void registerMachineDefinitions(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        GECreativeModeTabs.init();
        GEMachines.init();
    }

    /* -------------------------------------------------- Life Cycle Methods ---------------------------------------------------- */

    @Override
    public void onCommonSetupEvent(FMLCommonSetupEvent event) {
        GTItems.TOOL_DATA_STICK.get().attachComponents(new GEDataStickBehavior());
    }
}
