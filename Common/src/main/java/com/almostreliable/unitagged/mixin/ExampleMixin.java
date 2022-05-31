package com.almostreliable.unitagged.mixin;

import com.almostreliable.unitagged.UniTaggedCommon;
import com.almostreliable.unitagged.api.UniTaggedPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class ExampleMixin {

    @Inject(at = @At("HEAD"), method = "init()V")
    private void init(CallbackInfo info) {
        UniTaggedCommon.LOG.info("This line is printed by an example mod mixin from {}!",
                UniTaggedPlatform.INSTANCE.getPlatformName());
        UniTaggedCommon.LOG.info("MC Version: {}", Minecraft.getInstance().getVersionType());
        UniTaggedCommon.LOG.info("Classloader: {}", this.getClass().getClassLoader());
    }
}
