package com.almostreliable.unified.loot;

import com.almostreliable.unified.AlmostUnifiedCommon;
import com.almostreliable.unified.api.AlmostUnifiedRuntime;
import com.almostreliable.unified.api.ConfiguredUnificationHandler;
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
            var handlers = runtime.getConfiguredUnificationHandlers();

            boolean enableLootUnification = handlers
                    .stream()
                    .anyMatch(ConfiguredUnificationHandler::enableLootUnification);
            if (!enableLootUnification) {
                return;
            }

            var lootTableRegistry = registries.lookupOrThrow(Registries.LOOT_TABLE);

            lootTableRegistry
                    .listElements()
                    .forEach(holder -> unifyLoot(holder.value(), holder.key().location(), handlers));
        } catch (Exception e) {
            AlmostUnifiedCommon.LOGGER.error("Failed to unify loot", e);
        }
    }

    public static void unifyLoot(LootTable lootTable, ResourceLocation tableId, Collection<? extends ConfiguredUnificationHandler> configuredUnificationHandlers) {
        LootUnificationHandler lootUnificationHandler = LootUnificationHandler.cast(lootTable);

        Set<ConfiguredUnificationHandler> modifiedTable = new HashSet<>();
        for (ConfiguredUnificationHandler handler : configuredUnificationHandlers) {
            if (handler.enableLootUnification() && handler.shouldUnifyLootTable(tableId)) {
                if (lootUnificationHandler.almostunified$unify(handler)) {
                    modifiedTable.add(handler);
                }
            }
        }

        if (!modifiedTable.isEmpty()) {
            AlmostUnifiedCommon.LOGGER.info("Loot table '{}' was unified by: {}",
                    tableId,
                    modifiedTable.stream().map(ConfiguredUnificationHandler::getName).collect(
                            Collectors.joining(", ")));
        }
    }
}
