package com.almostreliable.unified.unification.loot;

import com.almostreliable.unified.api.unification.UnificationLookup;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

public interface LootUnificationHandler {

    static LootUnificationHandler cast(LootPool pool) {
        return (LootUnificationHandler) pool;
    }

    static LootUnificationHandler cast(LootTable table) {
        return (LootUnificationHandler) table;
    }

    boolean almostunified$unify(UnificationLookup lookup);
}
