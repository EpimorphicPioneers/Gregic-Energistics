package com.epimorphismmc.gregiceng.api.gui.wight;

import com.epimorphismmc.gregiceng.api.misc.IConfigurableAESlotList;

import appeng.api.stacks.AEKey;

import com.lowdragmc.lowdraglib.gui.ingredient.IGhostIngredientTarget;
import com.lowdragmc.lowdraglib.gui.ingredient.Target;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawSolidRect;

public abstract class ConfigSlotWidget<T extends AEKey> extends Widget
        implements IGhostIngredientTarget {
    protected static final int REMOVE_ID = 1000;
    protected static final int UPDATE_ID = 1001;
    protected static final int BLOCKED_OVERLAY_COLOR = 0x80404040;
    protected static final int SELECTION_OVERLAY_COLOR = -0x7f000001;
    protected final int index;
    protected final IConfigurableAESlotList<T> slotList;
    protected final Predicate<T> validator;
    protected T latestConfig;
    protected BooleanSupplier isBlocked = () -> false;

    public ConfigSlotWidget(
            IConfigurableAESlotList<T> slotList, int index, Position pos, Predicate<T> validator) {
        super(pos, new Size(18, 18));
        this.slotList = slotList;
        this.index = index;
        this.validator = validator;
    }

    public ConfigSlotWidget(
            IConfigurableAESlotList<T> slotList, int index, int x, int y, Predicate<T> validator) {
        this(slotList, index, new Position(x, y), validator);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawInForeground(
            @NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (isMouseOverElement(mouseX, mouseY)
                && getHoverElement(mouseX, mouseY) == this
                && gui != null
                && gui.getModularUIGui() != null) {
            drawTooltipTexts(mouseX, mouseY);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected void drawTooltipTexts(int mouseX, int mouseY) {
        gui.getModularUIGui()
                .setHoverTooltip(getHoverTexts(new ArrayList<>()), ItemStack.EMPTY, null, null);
    }

    public ConfigSlotWidget<T> setIsBlocked(BooleanSupplier isBlocked) {
        this.isBlocked = isBlocked;
        return this;
    }

    protected @Nullable T getConfig() {
        return slotList.getAESlot(index).getConfig();
    }

    protected void setConfig(@Nullable T config) {
        slotList.getAESlot(index).setConfig(config);
    }

    protected List<Component> getHoverTexts(List<Component> hoverTexts) {
        if (getConfig() == null) {
            if (isBlocked.getAsBoolean()) {

            } else {
                hoverTexts.add(Component.translatable("gtceu.gui.config_slot"));
                hoverTexts.add(Component.translatable("gtceu.gui.config_slot.set"));
                hoverTexts.add(Component.translatable("gtceu.gui.config_slot.remove"));
            }
            hoverTexts.addAll(tooltipTexts);
        }
        return hoverTexts;
    }

    @OnlyIn(Dist.CLIENT)
    protected static void drawBlockedOverlay(
            GuiGraphics graphics, int x, int y, int width, int height) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        drawSolidRect(graphics, x, y, width, height, BLOCKED_OVERLAY_COLOR);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
    }

    @OnlyIn(Dist.CLIENT)
    protected static void drawSelectionOverlay(
            GuiGraphics graphics, int x, int y, int width, int height) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        drawSolidRect(graphics, x, y, width, height, SELECTION_OVERLAY_COLOR);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
    }

    @Override
    public List<Target> getPhantomTargets(Object ingredient) {
        return Collections.emptyList();
    }
}
