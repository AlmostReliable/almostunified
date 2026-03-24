package com.almostreliable.unified.mixin;

import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;

import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = ContextAwareReloadListener.class, remap = false)
public interface ContextAwareReloadListenerAccessor {

    @Invoker("makeConditionalOps")
    ConditionalOps<JsonElement> au$makeConditionalOps();
}
