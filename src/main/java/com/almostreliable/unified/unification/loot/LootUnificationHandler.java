package com.almostreliable.unified.unification.loot;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import com.almostreliable.unified.api.unification.UnificationLookup;

public interface LootUnificationHandler {

    static LootUnificationHandler cast(LootPool pool) {
        return (LootUnificationHandler) pool;
    }

    static LootUnificationHandler cast(LootTable table) {
        return (LootUnificationHandler) table;
    }

    boolean almostunified$unify(UnificationLookup lookup);
}
