package com.almostreliable.unified.loot;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.AlmostUnifiedRuntime;
import com.almostreliable.unified.EmptyAlmostUnifiedRuntime;
import com.almostreliable.unified.api.UnifyHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class LootUnification {

    public static void unifyLoot(LootDataManager lootDataManager) {
        try {
            AlmostUnifiedRuntime runtime = AlmostUnified.getRuntime();
            if (runtime instanceof EmptyAlmostUnifiedRuntime) {
                return;
            }

            Collection<? extends UnifyHandler> unifyHandlers = runtime.getUnifyHandlers();

            boolean enableLootUnification = unifyHandlers.stream().anyMatch(UnifyHandler::enableLootUnification);
            if (!enableLootUnification) {
                return;
            }

            for (ResourceLocation tableId : lootDataManager.getKeys(LootDataType.TABLE)) {
                LootTable table = lootDataManager.getElement(LootDataType.TABLE, tableId);
                if (table != null) {
                    unifyLoot(tableId, table, unifyHandlers);
                }


            }
        } catch (Exception e) {
            AlmostUnified.LOG.error("Failed to unify loot", e);
        }
    }

    public static void unifyLoot(ResourceLocation tableId, LootTable table) {
        try {
            AlmostUnifiedRuntime runtime = AlmostUnified.getRuntime();
            if (runtime instanceof EmptyAlmostUnifiedRuntime) {
                return;
            }

            unifyLoot(tableId, table, runtime.getUnifyHandlers());
        } catch (Exception e) {
            AlmostUnified.LOG.error("Failed to unify loot", e);
        }
    }

    private static void unifyLoot(ResourceLocation tableId, LootTable table, Collection<? extends UnifyHandler> unifyHandlers) {
        LootUnifyHandler lootUnifyHandler = LootUnifyHandler.cast(table);

        Set<UnifyHandler> modifiedTable = new HashSet<>();
        for (UnifyHandler unifyHandler : unifyHandlers) {
            if (unifyHandler.enableLootUnification() && unifyHandler.shouldUnifyLootTable(tableId)) {
                if (lootUnifyHandler.almostunified$unify(unifyHandler)) {
                    modifiedTable.add(unifyHandler);
                }
            }
        }

        if (!modifiedTable.isEmpty()) {
            AlmostUnified.LOG.info("Loot table '{}' was unified by: {}",
                    tableId,
                    modifiedTable.stream().map(UnifyHandler::getName).collect(
                            Collectors.joining(", ")));
        }
    }
}
