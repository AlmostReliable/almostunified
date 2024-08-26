package com.almostreliable.unified.mixin.runtime;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;

import com.almostreliable.unified.AlmostUnifiedCommon;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

// inject after most mods but before KubeJS
@Mixin(value = RecipeManager.class, priority = 1_099)
public class RecipeManagerMixin {

    @Shadow @Final private HolderLookup.Provider registries;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
    private void almostunified$onRecipeReload(Map<ResourceLocation, JsonElement> recipes, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        try {
            AlmostUnifiedCommon.onRecipeManagerReload(recipes, registries);
        } catch (Exception e) {
            AlmostUnifiedCommon.LOGGER.error(e.getMessage(), e);
        }
    }

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"), remap = false)
    private void almostunified$onRecipeError(Map<ResourceLocation, JsonElement> recipes, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci, @Local ResourceLocation recipe) {
        AlmostUnifiedCommon.onRecipeManagerError(recipe);
    }

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("TAIL"))
    private void almostunified$onRecipeReloadEnd(Map<ResourceLocation, JsonElement> recipes, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        AlmostUnifiedCommon.onRecipeManagerEnd();
    }
}
