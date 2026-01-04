package com.almostreliable.unified.mixin.neoforge;

import com.google.gson.JsonElement;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = ContextAwareReloadListener.class, remap = false)
public interface ContextAwareReloadListenerAccessor {

    @Invoker("makeConditionalOps")
    ConditionalOps<JsonElement> au$makeConditionalOps();
}
