package com.epimorphismmc.gregiceng.common.item;

import com.epimorphismmc.gregiceng.common.machine.multiblock.part.appeng.CraftingIOBufferPartMachine;
import com.epimorphismmc.gregiceng.common.machine.multiblock.part.appeng.CraftingIOSlavePartMachine;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GEDataStickBehavior implements IInteractionItem, IAddInformation {
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        ItemStack stack = context.getItemInHand();
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }
        if (blockEntity instanceof IMachineBlockEntity machineBlockEntity) {
            MetaMachine machine = machineBlockEntity.getMetaMachine();
            if (machine instanceof CraftingIOBufferPartMachine) {
                stack.getOrCreateTag().putIntArray("pos", new int[] {pos.getX(), pos.getY(), pos.getZ()});
                return InteractionResult.SUCCESS;
            } else if (machine instanceof CraftingIOSlavePartMachine slave) {
                if (stack.hasTag()) {
                    if (stack.getOrCreateTag().contains("pos", Tag.TAG_INT_ARRAY)) {
                        int[] posArray = stack.getOrCreateTag().getIntArray("pos");
                        BlockPos bufferPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
                        slave.setIOBuffer(bufferPos);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return IInteractionItem.super.useOn(context);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltipComponents,
            TooltipFlag isAdvanced) {
        if (stack.hasTag()) {
            if (stack.getOrCreateTag().contains("pos", Tag.TAG_INT_ARRAY)) {
                int[] posArray = stack.getOrCreateTag().getIntArray("pos");
                tooltipComponents.add(Component.translatable(
                        "gregiceng.tooltip.buffer_bind",
                        Component.literal("" + posArray[0]).withStyle(ChatFormatting.LIGHT_PURPLE),
                        Component.literal("" + posArray[1]).withStyle(ChatFormatting.LIGHT_PURPLE),
                        Component.literal("" + posArray[2]).withStyle(ChatFormatting.LIGHT_PURPLE)));
            }
        }
    }
}
