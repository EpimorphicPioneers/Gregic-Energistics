package com.epimorphismmc.gregiceng.api.gui.wight;

import com.epimorphismmc.gregiceng.api.misc.IConfigurableAESlot;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.util.TextFormattingUtil;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.AEItemKey;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawItemStack;
import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawStringFixedCorner;

public class ItemAESlotWidget extends AESlotWidget<AEItemKey> {
    protected ItemStack stack = ItemStack.EMPTY;

    public ItemAESlotWidget(IConfigurableAESlot<AEItemKey> aeSlot, Position selfPosition) {
        super(aeSlot, selfPosition);
    }

    public ItemAESlotWidget(IConfigurableAESlot<AEItemKey> aeSlot, int x, int y) {
        super(aeSlot, x, y);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawInForeground(
            @NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (isMouseOverElement(mouseX, mouseY) && getHoverElement(mouseX, mouseY) == this) {
            if (!stack.isEmpty() && gui != null) {
                List<Component> tips = new ArrayList<>(getToolTips(DrawerHelper.getItemToolTip(stack)));
                tips.addAll(tooltipTexts);
                gui.getModularUIGui()
                        .setHoverTooltip(tips, stack, null, stack.getTooltipImage().orElse(null));
            } else {
                super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
            }
        } else {
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawInBackground(
            @NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        Position position = getPosition();
        GuiTextures.SLOT.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
        int stackX = position.x + 1;
        int stackY = position.y + 1;
        if (!stack.isEmpty()) {
            drawItemStack(graphics, stack, stackX, stackY, 0xFFFFFFFF, "");
            String amountStr = TextFormattingUtil.formatLongToCompactString(amount, 4);
            drawStringFixedCorner(graphics, amountStr, stackX + 17, stackY + 17, 16777215, true, 0.5f);
        }

        drawOverlay(graphics, mouseX, mouseY, partialTicks);
        if (isMouseOverElement(mouseX, mouseY) && getHoverElement(mouseX, mouseY) == this) {
            RenderSystem.colorMask(true, true, true, false);
            DrawerHelper.drawSolidRect(
                    graphics,
                    getPosition().x + 1,
                    getPosition().y + 1,
                    getSize().width - 2,
                    getSize().height - 2,
                    0x80FFFFFF);
            RenderSystem.colorMask(true, true, true, true);
        }
    }

    @Override
    protected void fromPacket(@Nullable FriendlyByteBuf buf) {
        if (buf != null) {
            this.stack = AEItemKey.fromPacket(buf).toStack();
        } else {
            this.stack = ItemStack.EMPTY;
        }
    }
}
