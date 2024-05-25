package com.almostreliable.unified.worldgen;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.api.UnifyLookup;
import com.almostreliable.unified.mixin.neoforge.OreConfigurationAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorldGenUnifier {

    private final RegistryAccess registryAccess;
    private final Registry<ConfiguredFeature<?, ?>> cfRegistry;
    private final Set<Holder.Reference<ConfiguredFeature<?, ?>>> processedFeatures = new HashSet<>();

    public WorldGenUnifier(RegistryAccess registryAccess) {
        this.registryAccess = registryAccess;
        this.cfRegistry = registryAccess.registryOrThrow(Registries.CONFIGURED_FEATURE);
    }

    public void process() {
        var lookup = AlmostUnified.getRuntime().getUnifyLookup();
        cfRegistry.holders().forEach(holder -> {
            if (handleConfiguredFeature(lookup, holder)) {
                processedFeatures.add(holder);
            }
        });
    }

    private boolean handleConfiguredFeature(UnifyLookup lookup, Holder<ConfiguredFeature<?, ?>> cfHolder) {
        if (!(cfHolder.value().config() instanceof OreConfiguration oreConfig)) {
            return false;
        }

        boolean changed = false;
        List<OreConfiguration.TargetBlockState> newTargetStates = new ArrayList<>(oreConfig.targetStates);

        var it = newTargetStates.listIterator();
        while (it.hasNext()) {
            var currentTargetState = it.next();
            if (handleTargetState(lookup, currentTargetState)) {
                changed = true;
                it.remove();
            }
        }

        if (changed) {
            ((OreConfigurationAccessor) oreConfig).almostunified$setTargets(newTargetStates);
        }

        return newTargetStates.isEmpty();
    }

    private boolean handleTargetState(UnifyLookup lookup, OreConfiguration.TargetBlockState targetState) {
        var blockHolder = targetState.state.getBlockHolder();
        if (!(blockHolder instanceof Holder.Reference<Block> ref)) {
            return false;
        }

        var blockId = ref.key().location();
        var replacement = lookup.getReplacementForItem(blockId);
        if (replacement == null || replacement.id().equals(blockId)) {
            return false;
        }

        Block replacementBlock = BuiltInRegistries.BLOCK.getOptional(replacement.id()).orElse(null);
        if (replacementBlock == null) {
            AlmostUnified.LOG.error(
                    "Trying to find replacement for block {} (Replacement: {}), but it does not exist.",
                    blockId,
                    replacement.id());
            return false;
        }

        return true;
    }

    public boolean shouldRemovePlacedFeature(Holder<PlacedFeature> placedFeature) {
        Holder<ConfiguredFeature<?, ?>> cfHolder = placedFeature.value().feature();
        if (cfHolder instanceof Holder.Reference<ConfiguredFeature<?, ?>> ref) {
            return processedFeatures.contains(ref);
        }

        return false;
    }
}
