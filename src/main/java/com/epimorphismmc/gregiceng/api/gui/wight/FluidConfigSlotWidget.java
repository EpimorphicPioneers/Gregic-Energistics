package com.epimorphismmc.gregiceng.api.gui.wight;

import com.epimorphismmc.gregiceng.api.misc.IConfigurableAESlotList;

import com.epimorphismmc.monomorphism.ae2.AEUtils;

import appeng.api.stacks.AEFluidKey;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.ingredient.Target;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static com.gregtechceu.gtceu.utils.GTUtil.getFluidFromContainer;

public class FluidConfigSlotWidget extends ConfigSlotWidget<AEFluidKey> {
    public static final int LOAD_PHANTOM_FLUID_STACK_FROM_NBT = 13;

    public FluidConfigSlotWidget(
            IConfigurableAESlotList<AEFluidKey> slotList,
            int index,
            Position pos,
            Predicate<AEFluidKey> validator) {
        super(slotList, index, pos, validator);
    }

    public FluidConfigSlotWidget(
            IConfigurableAESlotList<AEFluidKey> slotList,
            int index,
            int x,
            int y,
            Predicate<AEFluidKey> validator) {
        super(slotList, index, x, y, validator);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawInBackground(
            @NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        Position position = getPosition();
        GuiTextures.FLUID_SLOT.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
        GuiTextures.CONFIG_ARROW.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);

        if (isBlocked.getAsBoolean()) {
            drawBlockedOverlay(graphics, position.x + 1, position.y + 1, 16, 16);
        }

        int stackX = position.x + 1;
        int stackY = position.y + 1;
        var config = getConfig();
        if (config != null) {
            DrawerHelper.drawFluidForGui(
                    graphics, AEUtils.toFluidStack(config, 1), 1, stackX, stackY, 16, 16);
        }

        if (isMouseOverElement(mouseX, mouseY) && getHoverElement(mouseX, mouseY) == this) {
            drawSelectionOverlay(graphics, position.x + 1, position.y + 1, 16, 16);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY) && !isBlocked.getAsBoolean()) {
            if (button == 1) {
                // Right click to clear
                writeClientAction(REMOVE_ID, buf -> {});
            } else if (button == 0) {
                // Left click to set/select
                ItemStack hold = this.gui.getModularUIContainer().getCarried();
                FluidStack fluid = FluidTransferHelper.getFluidContained(hold);

                if (fluid != null) {
                    var key = AEFluidKey.of(fluid.getFluid(), fluid.getTag());
                    writeClientAction(UPDATE_ID, key::writeToPacket);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (id == REMOVE_ID) {
            if (getConfig() == null) return;
            setConfig(null);
        }
        if (id == UPDATE_ID) {
            handleUpdatePacket(buffer);
        }
        if (id == LOAD_PHANTOM_FLUID_STACK_FROM_NBT) {
            handleUpdatePacket(buffer);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        var newConfig = getConfig();
        if (newConfig != null) {
            if (!newConfig.equals(latestConfig)) {
                this.latestConfig = newConfig;
                writeUpdateInfo(UPDATE_ID, latestConfig::writeToPacket);
            }
        } else {
            if (latestConfig != null) {
                this.latestConfig = null;
                writeUpdateInfo(REMOVE_ID, buf -> {});
            }
        }
    }

    private void handleUpdatePacket(FriendlyByteBuf buffer) {
        var key = AEFluidKey.fromPacket(buffer);
        if (key.equals(getConfig()) || slotList.hasConfig(key) || !validator.test(key)) return;
        setConfig(key);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == REMOVE_ID) {
            setConfig(null);
        }
        if (id == UPDATE_ID) {
            var key = AEFluidKey.fromPacket(buffer);
            setConfig(key);
        }
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        var config = getConfig();
        if (config != null) {
            buffer.writeBoolean(true);
            config.writeToPacket(buffer);
            this.latestConfig = config;
        } else {
            buffer.writeBoolean(false);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        if (buffer.readBoolean()) {
            setConfig(AEFluidKey.fromPacket(buffer));
        } else {
            setConfig(null);
        }
    }

    @Override
    public List<Target> getPhantomTargets(Object ingredient) {
        if (getFluidFromContainer(ingredient) == null) {
            return Collections.emptyList();
        }
        Rect2i rectangle = toRectangleBox();
        return Lists.newArrayList(new Target() {

            @NotNull @Override
            public Rect2i getArea() {
                return rectangle;
            }

            @Override
            public void accept(@NotNull Object ingredient) {
                FluidStack stack = getFluidFromContainer(ingredient);

                if (stack != null) {
                    var key = AEFluidKey.of(stack.getFluid(), stack.getTag());
                    writeClientAction(LOAD_PHANTOM_FLUID_STACK_FROM_NBT, key::writeToPacket);
                }
            }
        });
    }
}
