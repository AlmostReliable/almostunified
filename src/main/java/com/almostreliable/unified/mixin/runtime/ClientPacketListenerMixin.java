package com.almostreliable.unified.mixin.runtime;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;

import com.almostreliable.unified.utils.ClientTagUpdateEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleUpdateTags", at = @At("RETURN"))
    private void almostunified$onClientTagsUpdate(ClientboundUpdateTagsPacket packet, CallbackInfo ci) {
        ClientTagUpdateEvent.invoke();
    }
}
