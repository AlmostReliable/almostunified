package com.almostreliable.unified.mixin.neoforge;

import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(OreConfiguration.class)
public interface OreConfigurationAccessor {

    @Accessor("targetStates")
    @Final
    @Mutable
    void almostunified$setTargets(List<OreConfiguration.TargetBlockState> targetStates);
}
