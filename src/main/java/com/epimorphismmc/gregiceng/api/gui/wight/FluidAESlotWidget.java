package com.epimorphismmc.gregiceng.api.gui.wight;

import appeng.api.stacks.AEFluidKey;
import com.epimorphismmc.gregiceng.api.misc.IConfigurableAESlot;
import com.epimorphismmc.monomorphism.ae2.AEUtils;
import com.epimorphismmc.monomorphism.client.utils.ClientUtils;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.util.TextFormattingUtil;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.utils.Position;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FluidAESlotWidget extends AESlotWidget<AEFluidKey> {
    protected FluidStack stack = FluidStack.empty();

    public FluidAESlotWidget(IConfigurableAESlot<AEFluidKey> aeSlot, Position selfPosition) {
        super(aeSlot, selfPosition);
    }

    public FluidAESlotWidget(IConfigurableAESlot<AEFluidKey> aeSlot, int x, int y) {
        super(aeSlot, x, y);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (isMouseOverElement(mouseX, mouseY) && getHoverElement(mouseX, mouseY) == this) {
            if (!stack.isEmpty() && gui != null) {
                List<Component> tooltips = new ArrayList<>();
                tooltips.add(FluidHelper.getDisplayName(stack));
                tooltips.add(Component.translatable("ldlib.fluid.amount", amount, amount).append(" " + FluidHelper.getUnit()));
                tooltips.add(Component.translatable("ldlib.fluid.temperature", FluidHelper.getTemperature(stack)));
                tooltips.add(Component.translatable(FluidHelper.isLighterThanAir(stack) ? "ldlib.fluid.state_gas" : "ldlib.fluid.state_liquid"));
                tooltips.addAll(getTooltipTexts());
                tooltips.addAll(tooltipTexts);
                gui.getModularUIGui().setHoverTooltip(tooltips, ItemStack.EMPTY, null, null);
            } else {
                super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
            }
        } else {
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        Position position = getPosition();
        GuiTextures.FLUID_SLOT.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
        int stackX = position.x + 1;
        int stackY = position.y + 1;
        if (!stack.isEmpty()) {
            DrawerHelper.drawFluidForGui(graphics, stack, 1, stackX, stackY, 16, 16);
            graphics.pose().pushPose();
            graphics.pose().scale(0.5F, 0.5F, 1);
            String s = TextFormattingUtil.formatLongToCompactStringBuckets(amount, 3) + "B";
            Font fontRenderer = ClientUtils.getFontRenderer();
            graphics.drawString(fontRenderer, s, (int) ((stackX + (getSizeWidth() / 3f)) * 2 - fontRenderer.width(s) + 21), (int) ((stackY + (getSizeHeight() / 3f) + 6) * 2), 0xFFFFFF, true);
            graphics.pose().popPose();
        }

        drawOverlay(graphics, mouseX, mouseY, partialTicks);
        if (isMouseOverElement(mouseX, mouseY) && getHoverElement(mouseX, mouseY) == this) {
            RenderSystem.colorMask(true, true, true, false);
            DrawerHelper.drawSolidRect(graphics, getPosition().x + 1, getPosition().y + 1, getSize().width - 2, getSize().height - 2, 0x80FFFFFF);
            RenderSystem.colorMask(true, true, true, true);
        }
    }

    @Override
    protected void fromPacket(@Nullable FriendlyByteBuf buf) {
        if (buf != null) {
            this.stack = AEUtils.toFluidStack(AEFluidKey.fromPacket(buf), 1);
        } else {
            this.stack = FluidStack.empty();
        }
    }
}
