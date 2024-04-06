package com.almostreliable.unified.mixin.loot;

import com.almostreliable.unified.api.UnifyLookup;
import com.almostreliable.unified.loot.LootUnifyHandler;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(LootPool.class)
public class LootPoolMixin implements LootUnifyHandler {

    @Shadow @Final private List<LootPoolEntryContainer> entries;

    @Override
    public boolean almostunified$unify(UnifyLookup lookup) {
        boolean unified = false;

        for (LootPoolEntryContainer entry : this.entries) {
            if (entry instanceof LootUnifyHandler handler) {
                unified |= handler.almostunified$unify(lookup);
            }
        }

        return unified;
    }
}
