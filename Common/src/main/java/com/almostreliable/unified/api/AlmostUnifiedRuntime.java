package com.almostreliable.unified.api;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * The runtime is the core of the Almost Unified mod.<br>
 * It stores all required information about tags, recipes, unification handlers, and configs.
 * <p>
 * The runtime is reconstructed every time the game reloads. Within the reconstruction process, all configs are reloaded,
 * plugin unifiers are collected, tag changes are applied, and all handlers are recreated.
 */
public interface AlmostUnifiedRuntime {

    /**
     * Returns a composition of all {@link ConfiguredUnificationHandler}s.
     * <p>
     * Because {@link ConfiguredUnificationHandler}s include config-specific settings, and are thus not composable, the
     * composition is returned as a {@link UnificationHandler}.
     *
     * @return the {@link ConfiguredUnificationHandler} composition as a {@link UnificationHandler}
     */
    UnificationHandler getUnificationHandler();

    /**
     * Returns an unmodifiable collection of all {@link ConfiguredUnificationHandler}s.
     *
     * @return the {@link ConfiguredUnificationHandler} collection
     */
    Collection<? extends ConfiguredUnificationHandler> getConfiguredUnificationHandlers();

    /**
     * Returns the {@link ConfiguredUnificationHandler} with the given name.
     * <p>
     * The name of a {@link ConfiguredUnificationHandler} is the name of the config file it was created from.
     *
     * @param name the name of the {@link ConfiguredUnificationHandler}
     * @return the {@link ConfiguredUnificationHandler} with the given name or null if not found
     */
    @Nullable
    ConfiguredUnificationHandler getUnificationHandler(String name);

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
