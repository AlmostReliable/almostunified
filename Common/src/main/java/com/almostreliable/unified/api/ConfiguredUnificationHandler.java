package com.almostreliable.unified.api;

/**
 * Marker interface for combining a {@link UnificationHandler} with its respective {@link UnificationSettings}.
 * <p>
 * Because {@link UnificationSettings} are specific to their respective {@link UnificationHandler}, and are thus not
 * composable, this interface should only be used when specific settings need to be checked.<br>
 * Unification operations usually involve all {@link UnificationHandler}s.
 */
public interface ConfiguredUnificationHandler extends UnificationHandler, UnificationSettings {}
