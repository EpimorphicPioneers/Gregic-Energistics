package com.epimorphismmc.gregiceng.common;

import com.epimorphismmc.gregiceng.GregicEng;
import com.epimorphismmc.gregiceng.common.data.GECreativeModeTabs;
import com.epimorphismmc.gregiceng.common.data.GEMachines;
import com.epimorphismmc.monomorphism.proxy.base.ICommonProxyBase;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import net.minecraft.resources.ResourceLocation;

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
}
