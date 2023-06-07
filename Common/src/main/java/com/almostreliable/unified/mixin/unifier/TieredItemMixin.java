package com.almostreliable.unified.mixin.unifier;

import com.almostreliable.unified.AlmostUnified;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TieredItem.class)
public class TieredItemMixin {

    @Shadow @Final private Tier tier;

    @Inject(method = "isValidRepairItem", at = @At("HEAD"), cancellable = true)
    private void unified$repairUnification(ItemStack stack, ItemStack repairCandidate, CallbackInfoReturnable<Boolean> cir) {
        AlmostUnified.getRuntime().getReplacementMap().ifPresent(replacementMap -> {
            if (replacementMap.isItemInUnifiedIngredient(tier.getRepairIngredient(), repairCandidate)) {
                cir.setReturnValue(true);
            }
        });
    }
}
