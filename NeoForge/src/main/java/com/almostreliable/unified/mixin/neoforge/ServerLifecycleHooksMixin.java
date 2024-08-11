package com.almostreliable.unified.mixin.neoforge;

import com.almostreliable.unified.AlmostUnifiedCommon;
import com.almostreliable.unified.worldgen.WorldGenBiomeModifier;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLifecycleHooks.class)
public class ServerLifecycleHooksMixin {

    @Inject(method = "runModifiers", at = @At("HEAD"), remap = false)
    private static void almostunified$injectRegistryAccess(MinecraftServer server, CallbackInfo ci) {
        var registryAccess = server.registryAccess();
        var biomeModifiers = registryAccess.registryOrThrow(NeoForgeRegistries.Keys.BIOME_MODIFIERS);
        for (var bm : biomeModifiers) {
            if (bm instanceof WorldGenBiomeModifier wgbm) {
                try {
                    WorldGenBiomeModifier.bindUnifier(wgbm, registryAccess);
                } catch (Exception e) {
                    var id = biomeModifiers.getKey(bm);
                    AlmostUnifiedCommon.LOGGER.error("Failed to bind registry access to biome modifier " + id, e);
                }
            }
        }
    }
}
