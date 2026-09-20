package com.almostreliable.unified.mixin.runtime;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.common.conditions.ConditionalOps;

import com.almostreliable.unified.AlmostUnifiedCommon;
import com.almostreliable.unified.core.ConditionalRecipeLinkFactory;

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
import java.util.function.Consumer;

// inject after most mods but before KubeJS
@Mixin(value = RecipeManager.class, priority = 1_099)
public class NeoForgeRecipeManagerMixin {

    @Shadow
    @Final
    private HolderLookup.Provider registries;

    /**
     * Runs the unification before NeoForge posts the event to modify the recipe JSONs.
     * The conditions of the recipes are not evaluated at this point, which is handled by the recipe link factory.
     */
    @WrapOperation(
        method = "prepare",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/packs/resources/SimpleJsonResourceReloadListener;scanDirectoryWithModifier(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/FileToIdConverter;Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/util/Map;Ljava/util/function/Consumer;)V"
        )
    )
    private <T> void almostunified$unifyRecipes(
        ResourceManager resourceManager, FileToIdConverter converter, DynamicOps<JsonElement> ops, Codec<T> codec,
        Map<Identifier, T> output, Consumer<Map<Identifier, JsonElement>> modifier, Operation<Void> original
    ) {
        original.call(resourceManager, converter, ops, codec, output, (Consumer<Map<Identifier, JsonElement>>) recipes -> {
            try {
                AlmostUnifiedCommon.onRecipeManagerReload(
                    recipes,
                    registries,
                    cache -> new ConditionalRecipeLinkFactory((ConditionalOps<JsonElement>) ops, cache)
                );
            } catch (Exception e) {
                AlmostUnifiedCommon.LOGGER.error(e.getMessage(), e);
            }

            modifier.accept(recipes);
        });
    }
}
