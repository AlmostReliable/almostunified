package com.almostreliable.unified.compat.viewer;

import com.almostreliable.unified.unification.recipe.RecipeLink;
import com.almostreliable.unified.utils.Utils;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This recipe is used to track which recipes were unified. It is NOT used for crafting.
 * Each tracker will hold one namespace with a list of recipes that were unified for it.
 */
public record ClientRecipeTracker(String namespace, Map<ResourceLocation, ClientRecipeLink> recipes)
        implements Recipe<RecipeInput> {

    public static final ResourceLocation ID = Utils.getRL("client_recipe_tracker");
    public static final String RECIPES = "recipes";
    public static final String NAMESPACE = "namespace";
    public static final int UNIFIED_FLAG = 1;
    public static final int DUPLICATE_FLAG = 2;
    public static final RecipeSerializer<ClientRecipeTracker> SERIALIZER = new Serializer();
    public static final RecipeType<ClientRecipeTracker> TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return ID.getPath();
        }
    };


    /**
     * Creates a raw string representation.
     *
     * @param isUnified   Whether the recipe was unified.
     * @param isDuplicate Whether the recipe had duplicates.
     * @param idPath      The path of the recipe.
     * @return String representation as: `flag$idPath`
     */
    private static String createRaw(boolean isUnified, boolean isDuplicate, String idPath) {
        int flag = 0;
        if (isUnified) flag |= UNIFIED_FLAG;
        if (isDuplicate) flag |= DUPLICATE_FLAG;
        return flag + "$" + idPath;
    }

    //<editor-fold defaultstate="collapsed" desc="Default recipe stuff. Ignore this. Forget this.">
    @Override
    public boolean matches(RecipeInput recipeInput, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput recipeInput, HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
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


    public List<String> getLinkStrings() {
        return recipes.values().stream().map(l -> createRaw(l.isUnified, l.isDuplicate, l.id.getPath())).toList();
    }

    public static class Serializer implements RecipeSerializer<ClientRecipeTracker> {

        /**
         * Codec for the recipe tracker. The recipe will look like this:
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
         * </pre>
         */
        public static final MapCodec<ClientRecipeTracker> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
                .group(
                        Codec.STRING.fieldOf("namespace").forGetter(ClientRecipeTracker::namespace),
                        Codec.list(Codec.STRING).fieldOf("recipes").forGetter(ClientRecipeTracker::getLinkStrings)
                )
                .apply(instance, Serializer::of));


        public static final StreamCodec<RegistryFriendlyByteBuf, ClientRecipeTracker> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public ClientRecipeTracker decode(RegistryFriendlyByteBuf buffer) {
                int size = buffer.readInt();
                String namespace = buffer.readUtf();

                ImmutableMap.Builder<ResourceLocation, ClientRecipeLink> builder = ImmutableMap.builder();
                for (int i = 0; i < size; i++) {
                    String raw = buffer.readUtf();
                    ClientRecipeLink clientRecipeLink = parseRaw(namespace, raw);
                    builder.put(clientRecipeLink.id(), clientRecipeLink);
                }

                return new ClientRecipeTracker(namespace, builder.build());
            }

            /**
             * Writes the tracker to the buffer. The namespace is written separately to save some bytes.
             * Buffer output will look like:
             * <pre>
             *     size
             *     namespace
             *     flag$recipePath
             *     flag$recipe2Path
             *     ...
             *     flag$recipeNPath
             * </pre>
             *
             * @param buffer The buffer to write to
             * @param recipe The recipe to write
             */
            @Override
            public void encode(RegistryFriendlyByteBuf buffer, ClientRecipeTracker recipe) {
                buffer.writeInt(recipe.recipes.size());
                buffer.writeUtf(recipe.namespace);
                for (ClientRecipeLink clientRecipeLink : recipe.recipes.values()) {
                    String raw = createRaw(clientRecipeLink.isUnified(),
                            clientRecipeLink.isDuplicate(),
                            clientRecipeLink.id().getPath());
                    buffer.writeUtf(raw);
                }
            }
        };

        @Override
        public MapCodec<ClientRecipeTracker> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ClientRecipeTracker> streamCodec() {
            return STREAM_CODEC;
        }

        private static ClientRecipeTracker of(String namespace, List<String> recipes) {
            ImmutableMap.Builder<ResourceLocation, ClientRecipeLink> builder = ImmutableMap.builder();

            for (String recipe : recipes) {
                ClientRecipeLink link = parseRaw(namespace, recipe);
                builder.put(link.id(), link);
            }

            return new ClientRecipeTracker(namespace, builder.build());
        }

        /**
         * Creates a {@link ClientRecipeLink} from a raw string for the given namespace.
         *
         * @param namespace The namespace to use.
         * @param raw       The raw string.
         * @return The client sided recipe link.
         */
        public static ClientRecipeLink parseRaw(String namespace, String raw) {
            String[] split = raw.split("\\$", 2);
            int flag = Integer.parseInt(split[0]);
            boolean isUnified = (flag & UNIFIED_FLAG) != 0;
            boolean isDuplicate = (flag & DUPLICATE_FLAG) != 0;
            return new ClientRecipeLink(
                    ResourceLocation.fromNamespaceAndPath(namespace, split[1]),
                    isUnified,
                    isDuplicate
            );
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
         * Creates a map with the namespace as key and the JSON recipe.
         * These recipes are used later in {@link Serializer}
         *
         * @return The map with the namespace as key and the JSON recipe.
         */
        public Map<ResourceLocation, JsonObject> compute() {
            Map<ResourceLocation, JsonObject> result = new HashMap<>();
            recipesByNamespace.forEach((namespace, recipes) -> {
                JsonObject json = new JsonObject();
                json.addProperty("type", ID.toString());
                json.addProperty(NAMESPACE, namespace);
                json.add(RECIPES, recipes);
                result.put(Utils.getRL(namespace), json);
            });
            return result;
        }
    }
}
