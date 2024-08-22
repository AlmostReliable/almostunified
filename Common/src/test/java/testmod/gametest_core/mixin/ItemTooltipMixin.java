package testmod.gametest_core.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemTooltipMixin {

    @Inject(method = "getTooltipLines", at = @At("TAIL"))
    private void auTest$injectTagsToItem(Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir) {
        var stack = (ItemStack) (Object) this;
        List<Component> l = cir.getReturnValue();
        l.add(Component.literal("---------------"));
        l.add(Component.literal("Tags: "));
        stack.getItemHolder().tags().map(tag -> tag.location().toString()).sorted(String::compareTo).forEach(s -> {
            l.add(Component.literal("- " + s).withStyle(ChatFormatting.GRAY));
        });
    }
}
