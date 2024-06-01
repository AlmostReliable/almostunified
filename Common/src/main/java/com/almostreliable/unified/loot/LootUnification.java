package com.almostreliable.unified.loot;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.api.AlmostUnifiedRuntime;
import com.almostreliable.unified.api.UnifyHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class LootUnification {

    public static void unifyLoot(AlmostUnifiedRuntime runtime, HolderLookup.Provider registries) {
        try {
            Collection<? extends UnifyHandler> unifyHandlers = runtime.getUnifyHandlers();

            boolean enableLootUnification = unifyHandlers.stream().anyMatch(UnifyHandler::enableLootUnification);
            if (!enableLootUnification) {
                return;
            }

            var lootTableRegistry = registries.lookupOrThrow(Registries.LOOT_TABLE);

            lootTableRegistry
                    .listElements()
                    .forEach(holder -> unifyLoot(holder.value(), holder.key().location(), unifyHandlers));
        } catch (Exception e) {
            AlmostUnified.LOG.error("Failed to unify loot", e);
        }
    }

    public static void unifyLoot(LootTable lootTable, ResourceLocation tableId, Collection<? extends UnifyHandler> unifyHandlers) {
        LootUnifyHandler lootUnifyHandler = LootUnifyHandler.cast(lootTable);

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
