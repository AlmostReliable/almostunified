package com.almostreliable.unified.loot;

import com.almostreliable.unified.api.UnifyLookup;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

public interface LootUnifyHandler {

    static LootUnifyHandler cast(LootPool pool) {
        return (LootUnifyHandler) pool;
    }

    static LootUnifyHandler cast(LootTable table) {
        return (LootUnifyHandler) table;
    }

    boolean almostunified$unify(UnifyLookup lookup);
}
