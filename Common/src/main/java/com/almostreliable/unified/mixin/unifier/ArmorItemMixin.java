package com.almostreliable.unified.mixin.unifier;

import com.almostreliable.unified.AlmostUnified;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorItem.class)
public class ArmorItemMixin {

    @Shadow @Final protected ArmorMaterial material;

    @Inject(method = "isValidRepairItem", at = @At("HEAD"), cancellable = true)
    private void unified$repairUnification(ItemStack stack, ItemStack repairCandidate, CallbackInfoReturnable<Boolean> cir) {
        if (AlmostUnified
                .getRuntime()
                .getReplacementMap()
                .isItemInUnifiedIngredient(material.getRepairIngredient(), repairCandidate)) {
            cir.setReturnValue(true);
        }
    }
}
