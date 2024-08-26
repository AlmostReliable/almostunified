package testmod.old.recipe;


// TODO I BROKE THEM! NEED TO FIX
public class RecipeContextImplTest {
    public static String mekaTest = """
        {
            "type": "mekanism:combining",
            "mainInput": { "amount": 8, "ingredient": { "tag": "c:raw_materials/tin" } },
            "extraInput": { "ingredient": { "tag": "c:cobblestone/normal" } },
            "output": { "item": "mekanism:tin_ore" }
        }
        """;

//    @Test
//    public void depthReplace_MekaTest() {
//        JsonObject json = new Gson().fromJson(mekaTest, JsonObject.class);
//        ReplacementMap map = new ReplacementMap(TagMapTests.testTagMap(), TestUtils.DEFAULT_UNIFY_CONFIG);
////        RecipeContextImpl context = new RecipeContextImpl(new ResourceLocation("test"), json, map);
////        JsonElement result = context.createResultReplacement(json.getAsJsonObject("output"));
////        assertNull(result);
//    }
}
