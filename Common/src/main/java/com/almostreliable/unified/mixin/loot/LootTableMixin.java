package com.almostreliable.unified.mixin.loot;

import com.almostreliable.unified.api.UnificationHandler;
import com.almostreliable.unified.loot.LootUnificationHandler;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(LootTable.class)
public class LootTableMixin implements LootUnificationHandler {
    @Shadow @Final private List<LootPool> pools;

    @Override
    public boolean almostunified$unify(UnificationHandler lookup) {
        boolean unified = false;
        for (LootPool pool : this.pools) {
            unified |= LootUnificationHandler.cast(pool).almostunified$unify(lookup);
        }

        return unified;
    }
}
