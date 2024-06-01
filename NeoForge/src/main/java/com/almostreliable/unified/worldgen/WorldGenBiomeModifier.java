package com.almostreliable.unified.worldgen;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.BuildConfig;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorldGenBiomeModifier implements BiomeModifier {

    public static final MapCodec<BiomeModifier> CODEC = MapCodec.unit(WorldGenBiomeModifier::new);
    public static final ResourceLocation UNKNOWN_BIOME_ID = new ResourceLocation(
            BuildConfig.MOD_ID, "unknown_biome_id");

    public static void bindUnifier(WorldGenBiomeModifier modifier, RegistryAccess registryAccess) {
        if (AlmostUnified.getStartupConfig().allowWorldGenUnification()) {
            WorldGenUnifier unifier = new WorldGenUnifier(registryAccess);
            unifier.process();
            modifier.unifier = unifier;
        }
    }

    @Nullable
    private WorldGenUnifier unifier;

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.AFTER_EVERYTHING) {
            return;
        }

        if (unifier == null) {
            return;
        }

        Map<GenerationStep.Decoration, List<Holder<PlacedFeature>>> removedFeatures = new LinkedHashMap<>();

        for (GenerationStep.Decoration dec : GenerationStep.Decoration.values()) {
            var features = builder.getGenerationSettings().getFeatures(dec);
            features.removeIf(feature -> {
                if (unifier.shouldRemovePlacedFeature(feature)) {
                    removedFeatures.computeIfAbsent(dec, $ -> new ArrayList<>()).add(feature);
                    return true;
                }

                return false;
            });
        }

        if (!removedFeatures.isEmpty()) {
            AlmostUnified.LOG.info("[WorldGen] Removed features from Biome {}:",
                    biome.unwrapKey().map(ResourceKey::location).orElse(UNKNOWN_BIOME_ID));
            removedFeatures.forEach((decoration, features) -> {
                String ids = features
                        .stream()
                        .flatMap(f -> f.unwrapKey().map(ResourceKey::location).stream())
                        .map(ResourceLocation::toString)
                        .collect(Collectors.joining(", "));

                AlmostUnified.LOG.info("[WorldGen]\t{}: {}", decoration.getName(), ids);
            });
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
