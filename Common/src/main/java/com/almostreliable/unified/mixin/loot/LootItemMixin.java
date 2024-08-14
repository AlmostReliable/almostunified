package com.almostreliable.unified.mixin.loot;

import com.almostreliable.unified.api.UnificationHandler;
import com.almostreliable.unified.loot.LootUnificationHandler;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LootItem.class)
public class LootItemMixin implements LootUnificationHandler {
    @Shadow @Final @Mutable private Holder<Item> item;

    @Override
    public boolean almostunified$unify(UnificationHandler lookup) {
        var replacement = lookup.getItemReplacement(item);
        if (replacement == null || item.value().equals(replacement.value())) {
            return false;
        }

        this.item = replacement.asHolderOrThrow();
        return true;
    }
}
