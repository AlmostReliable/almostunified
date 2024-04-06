package com.almostreliable.unified.mixin.loot;

import com.almostreliable.unified.api.UnifyLookup;
import com.almostreliable.unified.loot.LootUnifyHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LootItem.class)
public class LootItemMixin implements LootUnifyHandler {
    @Shadow @Final @Mutable private Holder<Item> item;

    @Override
    public boolean almostunified$unify(UnifyLookup lookup) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item.value());
        ResourceLocation replacement = lookup.getReplacementForItem(key);
        if (!key.equals(replacement)) {
            return false;
        }

        this.item = BuiltInRegistries.ITEM.getHolderOrThrow(ResourceKey.create(Registries.ITEM, replacement));
        return true;
    }
}
