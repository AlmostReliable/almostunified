package com.almostreliable.unified.recipe;

import com.almostreliable.unified.BuildConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * This recipe is used to track which recipes were unified. It is NOT used for crafting.
 * Each tracker will hold one namespace with a list of recipes that were unified for this namespace.
 */
public class ClientRecipeTracker implements Recipe<Container> {
    public static final ResourceLocation ID = new ResourceLocation(BuildConfig.MOD_ID, "client_recipe_tracker");
    public static final String RECIPES = "recipes";
    public static final String NAMESPACE = "namespace";
    public static final int UNIFIED_FLAG = 1;
    public static final int DUPLICATE_FLAG = 2;
    public static RecipeSerializer<ClientRecipeTracker> SERIALIZER = new Serializer();
    public static RecipeType<ClientRecipeTracker> TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return ID.getPath();
        }
    };

    private final ResourceLocation id;
    private final Map<ResourceLocation, ClientRecipeLink> recipes = new HashMap<>();
    private final String namespace;

    protected ClientRecipeTracker(ResourceLocation id, String namespace) {
        this.id = id;
        this.namespace = namespace;
    }

    /**
     * Create a raw string representation.
     *
     * @param isUnified   If the recipe was unified.
     * @param isDuplicate If the recipe was a duplicate.
     * @param idPath      The path of the recipe.
     * @return String representation as: `flag$idPath`
     */
    private static String createRaw(boolean isUnified, boolean isDuplicate, String idPath) {
        int flag = 0;
        if (isUnified) flag |= UNIFIED_FLAG;
        if (isDuplicate) flag |= DUPLICATE_FLAG;
        return flag + "$" + idPath;
    }

    /**
     * Creates a {@link ClientRecipeLink} from a raw string for given namespace
     *
     * @param namespace The namespace to use.
     * @param raw       The raw string.
     * @return The client sided recipe link.
     */
    private static ClientRecipeLink parseRaw(String namespace, String raw) {
        String[] split = raw.split("\\$", 2);
        int flag = Integer.parseInt(split[0]);
        boolean isUnified = (flag & UNIFIED_FLAG) != 0;
        boolean isDuplicate = (flag & DUPLICATE_FLAG) != 0;
        return new ClientRecipeLink(new ResourceLocation(namespace, split[1]), isUnified, isDuplicate);
    }

    //<editor-fold defaultstate="collapsed" desc="Default recipe stuff. Ignore this. Forget this.">
    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }
    //</editor-fold>

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    private void add(ClientRecipeLink clientRecipeLink) {
        recipes.put(clientRecipeLink.id(), clientRecipeLink);
    }

    @Nullable
    public ClientRecipeLink getLink(ResourceLocation recipeId) {
        return recipes.get(recipeId);
    }

    public record ClientRecipeLink(ResourceLocation id, boolean isUnified, boolean isDuplicate) {}

    public static class Serializer implements RecipeSerializer<ClientRecipeTracker> {

        /**
         * Read a recipe from a json file. Recipe will look like this:
         * <pre>
         * {@code
         * {
         *      "type": "almostunified:client_recipe_tracker",
         *      "namespace": "minecraft", // The namespace of the recipes.
         *      "recipes": [
         *          "flag$recipePath",
         *          "flag$recipe2Path",
         *          ...
         *          "flag$recipeNPath"
         *      ]
         * }
         * }
         * @param recipeId The id of the recipe for the tracker.
         * @param json    The json object.
         * @return The recipe tracker.
         */
        @Override
        public ClientRecipeTracker fromJson(ResourceLocation recipeId, JsonObject json) {
            String namespace = json.get(NAMESPACE).getAsString();
            JsonArray recipes = json.get(RECIPES).getAsJsonArray();
            ClientRecipeTracker tracker = new ClientRecipeTracker(recipeId, namespace);
            for (JsonElement element : recipes) {
                ClientRecipeLink clientRecipeLink = parseRaw(namespace, element.getAsString());
                tracker.add(clientRecipeLink);
            }
            return tracker;
        }

        @Override
        public ClientRecipeTracker fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int size = buffer.readInt();
            String namespace = buffer.readUtf();

            ClientRecipeTracker recipe = new ClientRecipeTracker(recipeId, namespace);
            for (int i = 0; i < size; i++) {
                String raw = buffer.readUtf();
                ClientRecipeLink clientRecipeLink = parseRaw(namespace, raw);
                recipe.add(clientRecipeLink);
            }
            return recipe;
        }

        /**
         * Will write our tracker to the buffer. Namespace is written separately to save some bytes.
         * Buffer output will look like:
         * <pre>
         *     size
         *     namespace
         *     flag$recipePath
         *     flag$recipe2Path
         *     ...
         *     flag$recipeNPath
         * @param buffer the buffer to write to
         * @param recipe the recipe to write
         */
        @Override
        public void toNetwork(FriendlyByteBuf buffer, ClientRecipeTracker recipe) {
            buffer.writeInt(recipe.recipes.size());
            buffer.writeUtf(recipe.namespace);
            for (ClientRecipeLink clientRecipeLink : recipe.recipes.values()) {
                String raw = createRaw(clientRecipeLink.isUnified(),
                        clientRecipeLink.isDuplicate(),
                        clientRecipeLink.id().getPath());
                buffer.writeUtf(raw);
            }
        }
    }

    public static class RawBuilder {

        private final Map<String, JsonArray> recipesByNamespace = new HashMap<>();

        public void add(RecipeLink recipe) {
            ResourceLocation recipeId = recipe.getId();
            JsonArray array = recipesByNamespace.computeIfAbsent(recipeId.getNamespace(), k -> new JsonArray());
            array.add(createRaw(recipe.isUnified(), recipe.hasDuplicateLink(), recipeId.getPath()));
        }

        /**
         * Creates a map with the namespace as key and the json recipe. These recipes are used later in {@link Serializer#fromJson(ResourceLocation, JsonObject)}
         *
         * @return The map with the namespace as key and the json recipe.
         */
        public Map<ResourceLocation, JsonObject> compute() {
            Map<ResourceLocation, JsonObject> result = new HashMap<>();
            recipesByNamespace.forEach((namespace, recipes) -> {
                JsonObject json = new JsonObject();
                json.addProperty("type", ID.toString());
                json.addProperty(NAMESPACE, namespace);
                json.add(RECIPES, recipes);
                result.put(new ResourceLocation(BuildConfig.MOD_ID, namespace), json);
            });
            return result;
        }
    }
}
