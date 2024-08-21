package com.almostreliable.unified.api;

import com.almostreliable.unified.api.unification.Placeholders;
import com.almostreliable.unified.api.unification.TagSubstitutions;
import com.almostreliable.unified.api.unification.UnificationLookup;
import com.almostreliable.unified.api.unification.UnificationSettings;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * The runtime is the core of the Almost Unified mod.<br>
 * It stores all required information about tags, recipes, unification handlers, and configs.
 * <p>
 * The runtime is reconstructed every time the game reloads. Within the reconstruction process, all configs are reloaded,
 * plugin unifiers are collected, tag changes are applied, and all handlers are recreated.
 *
 * @since 1.0.0
 */
public interface AlmostUnifiedRuntime {

    /**
     * Returns a composition of all {@link UnificationSettings}s.
     * <p>
     * Because {@link UnificationSettings}s include config-specific settings, and are thus not composable, the
     * composition is returned as a {@link UnificationLookup}.
     *
     * @return the {@link UnificationSettings} composition as a {@link UnificationLookup}
     */
    UnificationLookup getUnificationLookup();

    /**
     * Returns an unmodifiable collection of all {@link UnificationSettings}s.
     *
     * @return the {@link UnificationSettings} collection
     */
    Collection<? extends UnificationSettings> getUnificationSettings();

    /**
     * Returns the {@link UnificationSettings} with the given name.
     * <p>
     * The name of a {@link UnificationSettings} is the name of the config file it was created from.
     *
     * @param name the name of the {@link UnificationSettings}
     * @return the {@link UnificationSettings} with the given name or null if not found
     */
    @Nullable
    UnificationSettings getUnificationLookup(String name);

    /**
     * Returns the {@link TagSubstitutions} instance.
     * <p>
     * {@link TagSubstitutions} are defined in the {@code TagConfig}.
     *
     * @return the {@link TagSubstitutions}
     */
    TagSubstitutions getTagSubstitutions();

    /**
     * Returns the {@link Placeholders} instance.
     * <p>
     * {@link Placeholders} are defined in the {@code PlaceholderConfig}.
     *
     * @return the {@link Placeholders}
     */
    Placeholders getPlaceholders();
}
