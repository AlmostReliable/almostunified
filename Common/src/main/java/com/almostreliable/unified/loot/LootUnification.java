package com.almostreliable.unified.loot;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.api.AlmostUnifiedLookup;
import com.almostreliable.unified.api.AlmostUnifiedRuntime;
import com.almostreliable.unified.api.UnifyHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class LootUnification {

    public static void unifyLoot(Registry<LootTable> lootTableRegistry) {
        try {
            AlmostUnifiedRuntime runtime = AlmostUnifiedLookup.INSTANCE.getRuntime();
            if (runtime == null) {
                return;
            }

            Collection<? extends UnifyHandler> unifyHandlers = runtime.getUnifyHandlers();

            boolean enableLootUnification = unifyHandlers.stream().anyMatch(UnifyHandler::enableLootUnification);
            if (!enableLootUnification) {
                return;
            }

            lootTableRegistry.holders().forEach(holder -> unifyLoot(holder, unifyHandlers));
        } catch (Exception e) {
            AlmostUnified.LOG.error("Failed to unify loot", e);
        }
    }

    private static void unifyLoot(Holder.Reference<LootTable> lootTableHolder, Collection<? extends UnifyHandler> unifyHandlers) {
        LootUnifyHandler lootUnifyHandler = LootUnifyHandler.cast(lootTableHolder.value());
        var tableId = lootTableHolder.key().location();

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
