package com.almostreliable.unified.mixin.runtime;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.RecipeManager;

import com.almostreliable.unified.AlmostUnifiedCommon;
import com.almostreliable.unified.unification.recipe.RecipeLink;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.TreeMap;

// inject after most mods but before KubeJS
@Mixin(value = RecipeManager.class, priority = 1_099)
public class FabricRecipeManagerMixin {

    @Shadow
    @Final
    private HolderLookup.Provider registries;

    /**
     * Vanilla decodes the recipe JSONs right away, so there is no JSON map that could be unified.
     * The scan is run with a codec that only captures the JSON, which keeps all pre-filtering of other mods
     * (e.g. Fabric resource conditions) intact. Afterward, the captured JSONs are unified and decoded.
     */
    @WrapOperation(
        method = "prepare",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/packs/resources/SimpleJsonResourceReloadListener;scanDirectory(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/FileToIdConverter;Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/util/Map;)V"
        )
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> void almostunified$unifyRecipes(
        ResourceManager resourceManager, FileToIdConverter converter, DynamicOps<JsonElement> ops, Codec<T> codec,
        Map<Identifier, T> output, Operation<Void> original
    ) {
        Map<Identifier, JsonElement> recipes = new TreeMap<>();
        original.call(resourceManager, converter, ops, ExtraCodecs.JSON, (Map) recipes);

        try {
            AlmostUnifiedCommon.onRecipeManagerReload(recipes, registries, cache -> RecipeLink::of);
        } catch (Exception e) {
            AlmostUnifiedCommon.LOGGER.error(e.getMessage(), e);
        }

        recipes.forEach((id, json) -> codec
            .parse(ops, json)
            .ifSuccess(recipe -> output.putIfAbsent(id, recipe))
            .ifError(error -> {
                AlmostUnifiedCommon.onRecipeManagerError(id);
                AlmostUnifiedCommon.LOGGER.error("Parsing error loading recipe {}: {}", id, error.message());
            }));
    }
}
