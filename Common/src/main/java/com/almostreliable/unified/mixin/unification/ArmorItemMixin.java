package com.almostreliable.unified.mixin.unification;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import com.almostreliable.unified.api.AlmostUnified;
import com.almostreliable.unified.api.AlmostUnifiedRuntime;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorItem.class)
public class ArmorItemMixin {

    @Shadow @Final protected Holder<ArmorMaterial> material;

    @Inject(method = "isValidRepairItem", at = @At("HEAD"), cancellable = true)
    private void almostunified$onArmorRepair(ItemStack stack, ItemStack repairCandidate, CallbackInfoReturnable<Boolean> cir) {
        AlmostUnifiedRuntime runtime = AlmostUnified.INSTANCE.getRuntime();
        if (runtime == null) return;

        Ingredient repairIngredient = material.value().repairIngredient().get();
        if (runtime.getUnificationLookup().isUnifiedIngredientItem(repairIngredient, repairCandidate)) {
            cir.setReturnValue(true);
        }
    }
}
