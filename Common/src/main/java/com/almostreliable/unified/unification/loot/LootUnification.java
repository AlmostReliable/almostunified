package com.almostreliable.unified.unification.loot;

import com.almostreliable.unified.AlmostUnifiedCommon;
import com.almostreliable.unified.api.AlmostUnifiedRuntime;
import com.almostreliable.unified.api.unification.UnificationSettings;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class LootUnification {

    private LootUnification() {}

    public static void unifyLoot(AlmostUnifiedRuntime runtime, HolderLookup.Provider registries) {
        try {
            var handlers = runtime.getUnificationSettings();

            boolean enableLootUnification = handlers
                    .stream()
                    .anyMatch(UnificationSettings::shouldUnifyLoot);
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

    public static void unifyLoot(LootTable lootTable, ResourceLocation tableId, Collection<? extends UnificationSettings> unificationSettings) {
        LootUnificationHandler lootUnificationHandler = LootUnificationHandler.cast(lootTable);

        Set<UnificationSettings> modifiedTable = new HashSet<>();
        for (UnificationSettings handler : unificationSettings) {
            if (handler.shouldUnifyLoot() && handler.shouldIncludeLootTable(tableId)) {
                if (lootUnificationHandler.almostunified$unify(handler)) {
                    modifiedTable.add(handler);
                }
            }
        }

        if (!modifiedTable.isEmpty()) {
            AlmostUnifiedCommon.LOGGER.info("Loot table '{}' was unified by: {}",
                    tableId,
                    modifiedTable.stream().map(UnificationSettings::getName).collect(
                            Collectors.joining(", ")));
        }
    }
}
