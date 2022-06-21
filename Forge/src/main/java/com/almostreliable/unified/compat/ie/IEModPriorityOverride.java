package com.almostreliable.unified.compat.ie;

import blusunrize.immersiveengineering.api.IEApi;

import java.util.List;

/**
 * Overrides the mod preference config from IE. As an example, this is used for the IE recycler to generate
 * dynamically recipes.
 */
public class IEModPriorityOverride {
    public static Runnable overrideModPriorities(List<? extends String> modPriorities) {
        return () -> IEApi.modPreference = modPriorities;
    }
}
