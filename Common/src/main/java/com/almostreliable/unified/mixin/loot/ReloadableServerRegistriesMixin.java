package com.almostreliable.unified.mixin.loot;

import com.almostreliable.unified.loot.LootUnification;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ReloadableServerRegistries.class, priority = 1337)
public class ReloadableServerRegistriesMixin {

    @SuppressWarnings("unchecked")
    @Inject(method = "method_58279", at = @At("RETURN"))
    private static void almostunified$unifyLoot(LootDataType<?> lootDataType, ResourceManager resourceManager, RegistryOps<?> registryOps, CallbackInfoReturnable<WritableRegistry<?>> cir) {
        if (lootDataType == LootDataType.TABLE) {
            LootUnification.unifyLoot((Registry<LootTable>) cir.getReturnValue());
        }
    }
}
