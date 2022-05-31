package com.almostreliable.unitagged.mixin;

import com.almostreliable.unitagged.UniTagged;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.tags.TagManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {

    @Shadow @Final private TagManager tagManager;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void yoinkServerResources(RegistryAccess.Frozen frozen, Commands.CommandSelection commandSelection, int i, CallbackInfo ci) {
        UniTagged.getRuntime().updateTagManager(tagManager);
    }
}
