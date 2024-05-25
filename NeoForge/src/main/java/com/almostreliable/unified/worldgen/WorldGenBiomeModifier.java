package com.almostreliable.unified.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

import javax.annotation.Nullable;

public class WorldGenBiomeModifier implements BiomeModifier {

    public static final Codec<BiomeModifier> CODEC = Codec.unit(WorldGenBiomeModifier::new);

    public static void bindUnifier(WorldGenBiomeModifier modifier, RegistryAccess registryAccess) {
        WorldGenUnifier unifier = new WorldGenUnifier(registryAccess);
        unifier.process();
        modifier.unifier = unifier;
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

        for (GenerationStep.Decoration dec : GenerationStep.Decoration.values()) {
            var features = builder.getGenerationSettings().getFeatures(dec);
            features.removeIf(feature -> unifier.shouldRemovePlacedFeature(feature));
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
