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

import java.util.HashMap;
import java.util.Map;

public class ClientRecipeTracker implements Recipe<Container> {
    public static final ResourceLocation ID = new ResourceLocation(BuildConfig.MOD_ID, "client_recipe_tracker");
    public static final String RECIPE_ID = "id";
    public static final String RECIPES = "recipes";
    public static final String NAMESPACE = "namespace";
    public static RecipeSerializer<ClientRecipeTracker> SERIALIZER = new Serializer();

    public static final int UNIFIED_FLAG = 1;
    public static final int DUPLICATE_FLAG = 2;
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

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    private static String createRaw(boolean isUnified, boolean isDuplicate, String idPath) {
        int flag = 0;
        if (isUnified) flag |= UNIFIED_FLAG;
        if (isDuplicate) flag |= DUPLICATE_FLAG;
        return flag + "$" + idPath;
    }

    private static ClientRecipeLink parseRaw(String namespace, String raw) {
        String[] split = raw.split("\\$", 2);
        int flag = Integer.parseInt(split[0]);
        boolean isUnified = (flag & UNIFIED_FLAG) != 0;
        boolean isDuplicate = (flag & DUPLICATE_FLAG) != 0;
        return new ClientRecipeLink(new ResourceLocation(namespace, split[1]), isUnified, isDuplicate);
    }

    private void add(ClientRecipeLink clientRecipeLink) {
        recipes.put(clientRecipeLink.id(), clientRecipeLink);
    }

    public record ClientRecipeLink(ResourceLocation id, boolean isUnified, boolean isDuplicate) {}

    public static class Serializer implements RecipeSerializer<ClientRecipeTracker> {

        @Override
        public ClientRecipeTracker fromJson(ResourceLocation recipeId, JsonObject json) {
            String namespace = json.get(NAMESPACE).getAsString();
            JsonArray recipes = json.get(RECIPES).getAsJsonArray();
            ClientRecipeTracker recipe = new ClientRecipeTracker(recipeId, namespace);
            for (JsonElement element : recipes) {
                ClientRecipeLink clientRecipeLink = parseRaw(namespace, element.getAsString());
                recipe.add(clientRecipeLink);
            }
            return recipe;
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
