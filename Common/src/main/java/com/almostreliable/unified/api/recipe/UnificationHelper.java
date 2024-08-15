package com.almostreliable.unified.api.recipe;

import com.almostreliable.unified.api.TagSubstitutions;
import com.almostreliable.unified.api.UnificationHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javax.annotation.Nullable;

/**
 * Helper interface to aid in the unification of recipes.
 * <p>
 * This interface provides methods to unify elements within recipes. Unification involves converting elements to tags
 * or target items<br>
 * An instance of this interface is passed to {@link RecipeUnifier#unify(UnificationHelper, RecipeJson)} to assist in
 * the unification process of the given {@link RecipeJson}.
 * <p>
 * Implementations of this interface are expected to provide the logic for unification, typically based on predefined
 * lookup tables or constants. The methods provided by this interface handle various JSON structures, including
 * {@link JsonObject}s, {@link JsonArray}s, and individual {@link JsonElement}s.
 */
public interface UnificationHelper {

    /**
     * Returns the instance of the {@link UnificationHandler} this helper is based on.
     *
     * @return the {@link UnificationHandler} this helper is based on
     */
    UnificationHandler getUnificationHandler();

    /**
     * Fetches all entries of the given {@link RecipeJson} under the specified keys and unifies them as inputs.<br>
     * Entries treated as inputs will be converted to tags if possible.
     * <p>
     * The keys refer to top-level entries in the {@link RecipeJson}. This method requires at least one key to be
     * provided.<br>
     * To use default keys, refer to {@link RecipeConstants#DEFAULT_INPUT_KEYS} or see {@link GenericRecipeUnifier}.
     *
     * @param recipe the {@link RecipeJson} to fetch the input entries from
     * @param keys   the keys of the input entries to unify
     * @return true if any element was changed, false otherwise
     */
    boolean unifyInputs(RecipeJson recipe, String... keys);

    /**
     * Unifies a {@link JsonElement} as an input.<br>
     * Elements treated as inputs will be converted to tags if possible.
     * <p>
     * This method can unify {@link JsonObject}s and {@link JsonArray}s.<br>
     * The keys will be used for each nested element. If no keys are provided, it falls back to
     * {@link RecipeConstants#DEFAULT_INPUT_INNER_KEYS}.
     *
     * @param jsonElement the {@link JsonElement} to unify
     * @param keys        the keys to use
     * @return true if the {@link JsonElement} was changed, false otherwise
     */
    boolean unifyInputElement(JsonElement jsonElement, String... keys);

    /**
     * Unifies a {@link JsonArray} as an input.<br>
     * Elements treated as inputs will be converted to tags if possible.
     * <p>
     * The keys will be used for each nested element. If no keys are provided, it falls back to
     * {@link RecipeConstants#DEFAULT_INPUT_INNER_KEYS}.
     *
     * @param jsonArray the {@link JsonArray} to unify
     * @param keys      the keys to use
     * @return true if any element of the {@link JsonArray} was changed, false otherwise
     */
    boolean unifyInputArray(JsonArray jsonArray, String... keys);

    /**
     * Unifies a {@link JsonObject} as an input.<br>
     * Elements treated as inputs will be converted to tags if possible.
     * <p>
     * The keys will be used for each nested element. If no keys are provided, it falls back to
     * {@link RecipeConstants#DEFAULT_INPUT_INNER_KEYS}.
     *
     * @param jsonObject the {@link JsonObject} to unify
     * @param keys       the keys to use
     * @return true if any element of the {@link JsonObject} was changed, false otherwise
     */
    boolean unifyInputObject(JsonObject jsonObject, String... keys);

    /**
     * Unifies a {@link JsonObject} as a tag input.<br>
     * Tag inputs are only changed if they have an associated {@link TagSubstitutions} entry.
     *
     * @param jsonObject the {@link JsonObject} to unify
     * @return true if the tag input was changed, false otherwise
     */
    boolean unifyInputTag(JsonObject jsonObject);

    /**
     * Unifies a {@link JsonObject} as an item input.<br>
     * The item will be converted to a tag if possible.
     *
     * @param jsonObject the {@link JsonObject} to unify
     * @return true if the item input was changed, false otherwise
     */
    boolean unifyInputItem(JsonObject jsonObject);

    /**
     * Fetches all entries of the given {@link RecipeJson} under the specified keys and unifies them as
     * outputs.<br>
     * Entries treated as outputs will be converted to target items. If the entry is a tag, it will be converted
     * to the target item of the tag.
     * <p>
     * The keys refer to top-level entries in the {@link RecipeJson}. This method requires at least one key to be
     * provided.<br>
     * To use default keys, refer to {@link RecipeConstants#DEFAULT_OUTPUT_KEYS} or see {@link GenericRecipeUnifier}.
     *
     * @param recipe the {@link RecipeJson} to fetch the output entries from
     * @param keys   the keys of the output entries to unify
     * @return true if any element was changed, false otherwise
     */
    boolean unifyOutputs(RecipeJson recipe, String... keys);

    /**
     * Fetches all entries of the given {@link RecipeJson} under the specified keys and unifies them as
     * outputs.<br>
     * Entries treated as outputs will be converted to target items. If the entry is a tag and tagsToItems is true,
     * it will be converted to the target item of the tag.
     * <p>
     * The keys refer to top-level entries in the {@link RecipeJson}. This method requires at least one key to be
     * provided.<br>
     * To use default keys, refer to {@link RecipeConstants#DEFAULT_OUTPUT_KEYS} or see {@link GenericRecipeUnifier}.
     *
     * @param recipe      the {@link RecipeJson} to fetch the output entries from
     * @param tagsToItems if true, tags will be converted to target items
     * @param keys        the keys of the output entries to unify
     * @return true if any element was changed, false otherwise
     */
    boolean unifyOutputs(RecipeJson recipe, boolean tagsToItems, String... keys);

    /**
     * Unifies a {@link JsonElement} as an output.<br>
     * Elements treated as outputs will be converted to target items. If the element is a tag and tagsToItems is true,
     * it will be converted to the target item of the tag.
     * <p>
     * This method can unify {@link JsonObject}s and {@link JsonArray}s.<br>
     * The keys will be used for each nested element. If no keys are provided, it falls back to
     * {@link RecipeConstants#DEFAULT_OUTPUT_INNER_KEYS}.
     *
     * @param jsonElement the {@link JsonElement} to unify
     * @param tagsToItems if true, tags will be converted to target items
     * @param keys        the keys to use
     * @return true if the {@link JsonElement} was changed, false otherwise
     */
    boolean unifyOutputElement(JsonElement jsonElement, boolean tagsToItems, String... keys);

    /**
     * Unifies a {@link JsonArray} as an output.<br>
     * Elements treated as outputs will be converted to target items. If the element is a tag and tagsToItems is true,
     * it will be converted to the target item of the tag.
     * <p>
     * The keys will be used for each nested element. If no keys are provided, it falls back to
     * {@link RecipeConstants#DEFAULT_OUTPUT_INNER_KEYS}.
     *
     * @param jsonArray   the {@link JsonArray} to unify
     * @param tagsToItems if true, tags will be converted to target items
     * @param keys        the keys to use
     * @return true if the {@link JsonArray} was changed, false otherwise
     */
    boolean unifyOutputArray(JsonArray jsonArray, boolean tagsToItems, String... keys);

    /**
     * Unifies a {@link JsonObject} as an output.<br>
     * Elements treated as outputs will be converted to target items. If the element is a tag and tagsToItems is true,
     * it will be converted to the target item of the tag.
     * <p>
     * The keys will be used for each nested element. If no keys are provided, it falls back to
     * {@link RecipeConstants#DEFAULT_OUTPUT_INNER_KEYS}.
     *
     * @param jsonObject  the {@link JsonObject} to unify
     * @param tagsToItems if true, tags will be converted to target items
     * @param keys        the keys to use
     * @return true if the {@link JsonObject} was changed, false otherwise
     */
    boolean unifyOutputObject(JsonObject jsonObject, boolean tagsToItems, String... keys);

    /**
     * Unifies a {@link JsonObject} as a tag output.<br>
     * If tagsToItems is true, it will be converted to the target item of the tag. If tagsToItems is false, it
     * will only be changed if the tag has an associated {@link TagSubstitutions} entry.
     *
     * @param jsonObject  the {@link JsonObject} to unify
     * @param tagsToItems if true, the tag will be converted to the target item
     * @return true if the tag was changed, false otherwise
     */
    boolean unifyOutputTag(JsonObject jsonObject, boolean tagsToItems);

    /**
     * Unifies a {@link JsonObject} as an item output.<br>
     * The item will be converted to the target item of the tag if possible.
     * <p>
     * This uses the default keys {@link RecipeConstants#ITEM} and {@link RecipeConstants#ID}.
     *
     * @param jsonObject the {@link JsonObject} to unify
     * @return true if the item output was changed, false otherwise
     */
    boolean unifyOutputItem(JsonObject jsonObject);

    /**
     * Unifies a {@link JsonObject} as an item output.<br>
     * The item will be converted to the target item of the tag if possible.
     *
     * @param jsonObject the {@link JsonObject} to unify
     * @param key        the key of the output entry to unify
     * @return true if the item output was changed, false otherwise
     */
    boolean unifyOutputItem(JsonObject jsonObject, String key);

    /**
     * Handles the output item replacement.
     * <p>
     * It needs to be ensured that the passed {@link JsonPrimitive} is an item.
     *
     * @param jsonPrimitive the {@link JsonPrimitive} to handle
     * @return the replacement {@link JsonPrimitive} or null if no replacement was found
     */
    @Nullable
    JsonPrimitive handleOutputItemReplacement(JsonPrimitive jsonPrimitive);
}
