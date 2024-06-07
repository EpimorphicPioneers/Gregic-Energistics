package com.epimorphismmc.gregiceng.common.machine.multiblock.part.appeng;

import com.epimorphismmc.gregiceng.GregicEng;
import com.epimorphismmc.gregiceng.api.machine.trait.WrappedRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CraftingIOSlavePartMachine extends TieredIOPartMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CraftingIOSlavePartMachine.class, TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private BlockPos pos;
    private final WrappedRecipeHandlerTrait<Ingredient> itemInputHandler;
    private final WrappedRecipeHandlerTrait<Ingredient> itemOutputHandler;
    private final WrappedRecipeHandlerTrait<FluidIngredient> fluidInputHandler;
    private final WrappedRecipeHandlerTrait<FluidIngredient> fluidOutputHandler;
    private final WrappedRecipeHandlerTrait<Ingredient> shareItemHandler;
    private final WrappedRecipeHandlerTrait<FluidIngredient> shareFluidHandler;
    private final WrappedRecipeHandlerTrait<Ingredient> circuitHandler;

    public CraftingIOSlavePartMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.LuV, IO.BOTH);
        this.itemInputHandler = new WrappedRecipeHandlerTrait<>(IO.IN, ItemRecipeCapability.CAP);
        this.itemOutputHandler = new WrappedRecipeHandlerTrait<>(IO.OUT, ItemRecipeCapability.CAP);
        this.fluidInputHandler = new WrappedRecipeHandlerTrait<>(IO.IN, FluidRecipeCapability.CAP);
        this.fluidOutputHandler = new WrappedRecipeHandlerTrait<>(IO.OUT, FluidRecipeCapability.CAP);
        this.shareFluidHandler = new WrappedRecipeHandlerTrait<>(IO.IN, FluidRecipeCapability.CAP);
        this.shareItemHandler = new WrappedRecipeHandlerTrait<>(IO.IN, ItemRecipeCapability.CAP);
        this.circuitHandler = new WrappedRecipeHandlerTrait<>(IO.IN, ItemRecipeCapability.CAP);
    }

    private boolean setIOBuffer(BlockPos pos) {
        if (pos == null) return false;
        if (MetaMachine.getMachine(getLevel(), pos) instanceof CraftingIOBufferPartMachine) {
            this.pos = pos;
            itemInputHandler.setHandlerSupplier(() -> getIOBuffer().recipeHandler.getItemInputHandler());
            itemOutputHandler.setHandlerSupplier(() -> getIOBuffer().recipeHandler.getItemOutputHandler());
            fluidInputHandler.setHandlerSupplier(() -> getIOBuffer().recipeHandler.getFluidInputHandler());
            fluidOutputHandler.setHandlerSupplier(() -> getIOBuffer().recipeHandler.getFluidOutputHandler());
            shareFluidHandler.setHandlerSupplier(() -> getIOBuffer().shareTank);
            shareItemHandler.setHandlerSupplier(() -> getIOBuffer().shareInventory);
            circuitHandler.setHandlerSupplier(() -> getIOBuffer().circuitInventory);
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    private CraftingIOBufferPartMachine getIOBuffer() {
        if (pos == null) return null;
        if (MetaMachine.getMachine(getLevel(), pos) instanceof CraftingIOBufferPartMachine buffer) {
            return buffer;
        } else {
            this.pos = null;
            return null;
        }
    }

    @Override
    public MetaMachine self() {
        var buffer = getIOBuffer();
        return buffer != null ? buffer.self() : super.self();
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        var buffer = getIOBuffer();
        return buffer != null && super.shouldOpenUI(player, hand, hit);
    }

    @Override
    public @Nullable ModularUI createUI(Player entityPlayer) {
        GregicEng.logger().warn("'createUI' of the Crafting IO Slave was incorrectly called!");
        return null;
    }

    @Override
    public List<IRecipeHandlerTrait> getRecipeHandlers() {
        return List.of(itemInputHandler, itemOutputHandler, fluidInputHandler, fluidOutputHandler, shareItemHandler, shareFluidHandler, circuitHandler);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
