package com.almostreliable.unified.mixin.loot;

import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

import com.almostreliable.unified.api.unification.UnificationLookup;
import com.almostreliable.unified.unification.loot.LootUnificationHandler;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(CompositeEntryBase.class)
public class CompositeEntryBaseMixin implements LootUnificationHandler {
    @Shadow @Final protected List<LootPoolEntryContainer> children;

    @Override
    public boolean almostunified$unify(UnificationLookup lookup) {
        boolean unified = false;

        for (LootPoolEntryContainer child : children) {
            if (child instanceof LootUnificationHandler handler) {
                unified |= handler.almostunified$unify(lookup);
            }
        }

        return unified;
    }
}
