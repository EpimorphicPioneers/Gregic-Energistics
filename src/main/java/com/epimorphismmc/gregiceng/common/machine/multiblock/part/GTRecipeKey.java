package com.epimorphismmc.gregiceng.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public record GTRecipeKey(ResourceLocation id, int duration, long eut) {

    GTRecipeKey(GTRecipe recipe) {
        this(recipe.id, recipe.duration, RecipeHelper.getInputEUt(recipe) + RecipeHelper.getOutputEUt(recipe));
    }

    public boolean matches(GTRecipe recipe) {
        return duration == recipe.duration
            && id.equals(recipe.id)
            && eut == RecipeHelper.getInputEUt(recipe) + RecipeHelper.getOutputEUt(recipe);
    }

    public static GTRecipeKey create(GTRecipe recipe) {
        return new GTRecipeKey(recipe);
    }

    public static GTRecipeKey fromTag(CompoundTag tag) {
        return new GTRecipeKey(
            ResourceLocation.tryParse(tag.getString("id")),
            tag.getInt("dur"),
            tag.getLong("eut")
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id.toString());
        tag.putInt("dur", duration);
        tag.putLong("eut", eut);
        return tag;
    }
}
