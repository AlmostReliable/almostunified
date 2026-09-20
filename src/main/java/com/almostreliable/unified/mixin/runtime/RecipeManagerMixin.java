package com.almostreliable.unified.mixin.runtime;

import net.minecraft.world.item.crafting.RecipeManager;

import com.almostreliable.unified.AlmostUnifiedCommon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Inject(method = "apply", at = @At("TAIL"))
    private void almostunified$onRecipeReloadEnd(CallbackInfo ci) {
        AlmostUnifiedCommon.onRecipeManagerEnd();
    }
}
