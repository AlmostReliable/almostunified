package com.almostreliable.unified.mixin;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.google.common.collect.ImmutableMap;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

@SuppressWarnings("unused")
public class AlmostMixinPlugin implements IMixinConfigPlugin {

    private static final BooleanSupplier TRUE = () -> true;
    private static final Map<String, BooleanSupplier> CONDITIONS = ImmutableMap.of(
            "com.almostreliable.unified.mixin.Jei10RecipeLayoutMixin", modLoaded("jei"),
            "com.almostreliable.unified.mixin.Jei9RecipeLayoutMixin", modLoaded("jei")
    );

    private static BooleanSupplier modLoaded(String id) {
        return () -> AlmostUnifiedPlatform.INSTANCE.isModLoaded(id);
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @SuppressWarnings("ReturnOfNull")
    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return CONDITIONS.getOrDefault(mixinClassName, TRUE).getAsBoolean();
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @SuppressWarnings("ReturnOfNull")
    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
