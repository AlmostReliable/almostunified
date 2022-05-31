package com.almostreliable.unitagged;

import com.almostreliable.unitagged.api.UniTaggedPlatform;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UniTaggedCommon {

    public static final Logger LOG = LogManager.getLogger(BuildConfig.MOD_NAME);

    public static void init() {
        LOG.info("Hello from Common init on {}! we are currently in a {} environment!",
                UniTaggedPlatform.INSTANCE.getPlatformName(),
                UniTaggedPlatform.INSTANCE.isDevelopmentEnvironment() ? "development" : "production");
        LOG.info("Diamond Item >> {}", Registry.ITEM.getKey(Items.DIAMOND));
    }
}
