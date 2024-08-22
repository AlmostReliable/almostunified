package com.almostreliable.unified.api.unification;

import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * Helper for handling placeholders in configs.
 * <p>
 * Placeholders are used to replace specific patterns in config values with a set of values to easily cover all possible
 * combinations of all values. Placeholders are in the format of {@code {placeholder}}.
 *
 * @since 1.0.0
 */
public interface Placeholders {

    /**
     * Applies the placeholders to given string.
     * <p>
     * The given string is expected to contain an arbitrary number of placeholders or no placeholders at all.
     * <p>
     * This method replaces all contained placeholders with all combinations of possible values. If string doesn't
     * contain any placeholders, the same string will be returned as the only element in the returned collection.
     *
     * @param str the string to apply the placeholders to
     * @return a collection containing all combinations of applied placeholder values
     */
    Collection<String> apply(String str);

    /**
     * Returns all placeholders as a collection.
     *
     * @return a collection containing all placeholders
     */
    Collection<String> getPlaceholders();

    /**
     * Returns all possible replacements for given placeholder.
     * <p>
     * The possible replacement values are ensured to be unique.
     *
     * @param placeholder the placeholder to get replacements for
     * @return a collection containing all possible replacements for the given placeholder
     */
    Collection<String> getReplacements(String placeholder);

    /**
     * Passes each placeholder and its possible replacements to the given consumer.
     *
     * @param consumer the consumer to pass each placeholder and its possible replacements to
     */
    void forEach(BiConsumer<String, Collection<String>> consumer);
}
