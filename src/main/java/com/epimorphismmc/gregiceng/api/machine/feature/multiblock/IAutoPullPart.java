package com.epimorphismmc.gregiceng.api.machine.feature.multiblock;

import com.epimorphismmc.gregiceng.api.gui.GEGuiTextures;

import com.epimorphismmc.monomorphism.machine.fancyconfigurator.LongInputFancyConfigurator;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface IAutoPullPart extends IControllable, IMultiPart {

    long getMinPullAmount();

    void setMinPullAmount(long minPullAmount);

    IGuiTexture getConfiguratorOverlay();

    @Override
    default void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                        GEGuiTextures.BUTTON_AUTO_PULL_ME.getSubTexture(0, 0, 1, 0.5),
                        GEGuiTextures.BUTTON_AUTO_PULL_ME.getSubTexture(0, 0.5, 1, 0.5),
                        this::isWorkingEnabled,
                        (clickData, pressed) -> setWorkingEnabled(pressed))
                .setTooltipsSupplier(pressed -> List.of(Component.translatable(
                        pressed
                                ? "gui.gregiceng.auto_pull_me.desc.enabled"
                                : "gui.gregiceng.auto_pull_me.desc.disabled"))));
        configuratorPanel.attachConfigurators(new LongInputFancyConfigurator(
                "gui.gregiceng.min_pull_amount",
                getConfiguratorOverlay(),
                this::getMinPullAmount,
                this::setMinPullAmount));
    }
}
