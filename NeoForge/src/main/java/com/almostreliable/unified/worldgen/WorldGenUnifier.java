package com.almostreliable.unified.worldgen;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.api.AlmostUnifiedLookup;
import com.almostreliable.unified.api.UnifyLookup;
import com.almostreliable.unified.mixin.neoforge.OreConfigurationAccessor;
import com.almostreliable.unified.utils.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorldGenUnifier {
    public static final ResourceLocation UNKNOWN_FEATURE_ID = Utils.getRL("unknown_feature_id");
    private final Registry<ConfiguredFeature<?, ?>> cfRegistry;
    private final Set<Holder.Reference<ConfiguredFeature<?, ?>>> featuresToRemove = new HashSet<>();

    public WorldGenUnifier(RegistryAccess registryAccess) {
        this.cfRegistry = registryAccess.registryOrThrow(Registries.CONFIGURED_FEATURE);
    }

    public void process() {
        var lookup = AlmostUnifiedLookup.INSTANCE.getRuntimeOrThrow().getUnifyLookup();
        cfRegistry.holders().forEach(holder -> {
            switch (handleConfiguredFeature(lookup, holder)) {
                case SAME -> {
                    // do nothing
                }
                case REMOVE -> {
                    AlmostUnified.LOG.info("[WorldGen] Mark ConfiguredFeature '{}' for removal:",
                            holder.unwrapKey().map(ResourceKey::location).orElse(UNKNOWN_FEATURE_ID));
                    featuresToRemove.add(holder);
                }
                case CHANGE -> {
                    AlmostUnified.LOG.info("[WorldGen] Changed ConfiguredFeature '{}':",
                            holder.unwrapKey().map(ResourceKey::location).orElse(UNKNOWN_FEATURE_ID));
                }
            }
        });
    }

    private Result handleConfiguredFeature(UnifyLookup lookup, Holder<ConfiguredFeature<?, ?>> cfHolder) {
        if (!(cfHolder.value().config() instanceof OreConfiguration oreConfig)) {
            return Result.SAME;
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
            return newTargetStates.isEmpty() ? Result.REMOVE : Result.CHANGE;
        }

        return Result.SAME;
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
            return featuresToRemove.contains(ref);
        }

        return false;
    }

    private enum Result {
        SAME,
        CHANGE,
        REMOVE
    }
}
