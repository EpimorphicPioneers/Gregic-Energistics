package com.epimorphismmc.gregiceng.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class WrappedRecipeHandlerTrait<K> implements IRecipeHandlerTrait<K> {
    private final IO handlerIO;
    private final RecipeCapability<K> capability;

    @Getter
    protected List<Runnable> listeners = new ArrayList<>();

    @Setter
    private Supplier<IRecipeHandlerTrait<K>> handlerSupplier;

    public WrappedRecipeHandlerTrait(IO handlerIO, RecipeCapability<K> capability) {
        this.handlerIO = handlerIO;
        this.capability = capability;
    }

    @Override
    public List<K> handleRecipeInner(
            IO io, GTRecipe recipe, List<K> left, @Nullable String slotName, boolean simulate) {
        var handler = handlerSupplier.get();
        if (handler != null) {
            return handler.handleRecipeInner(io, recipe, left, slotName, simulate);
        }
        return left;
    }

    @Override
    public List<Object> getContents() {
        var handler = handlerSupplier.get();
        if (handler != null) {
            return handler.getContents();
        }
        return Collections.emptyList();
    }

    @Override
    public double getTotalContentAmount() {
        var handler = handlerSupplier.get();
        if (handler != null) {
            return handler.getTotalContentAmount();
        }
        return 0;
    }

    @Override
    public RecipeCapability<K> getCapability() {
        return capability;
    }

    @Override
    public IO getHandlerIO() {
        return handlerIO;
    }

    @Override
    public ISubscription addChangedListener(Runnable listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    @Override
    public int getPriority() {
        var handler = handlerSupplier.get();
        if (handler != null) {
            return handler.getPriority();
        }
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean isDistinct() {
        var handler = handlerSupplier.get();
        if (handler != null) {
            return handler.isDistinct();
        }
        return false;
    }

    @Override
    public void preWorking(IRecipeCapabilityHolder holder, IO io, GTRecipe recipe) {
        var handler = handlerSupplier.get();
        if (handler != null) {
            handler.preWorking(holder, io, recipe);
        }
    }

    @Override
    public void postWorking(IRecipeCapabilityHolder holder, IO io, GTRecipe recipe) {
        var handler = handlerSupplier.get();
        if (handler != null) {
            handler.postWorking(holder, io, recipe);
        }
    }
}
