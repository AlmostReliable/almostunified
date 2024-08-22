package testmod.old.util;


public class JsonCompareTest {

    private static final String EXPERIENCE = "experience";
    private static final String COOKING_TIME = "cookingtime";

//    @Test
//    public void simpleCompareFirst() {
//        JsonObject first = TestUtils.json(TestUtils.Recipes.SMELTING, j -> j.addProperty(EXPERIENCE, 0.2));
//        JsonObject second = TestUtils.json(TestUtils.Recipes.SMELTING); // 0.1 experience
//
//        Map<String, JsonCompare.Rule> rules = new LinkedHashMap<>();
//        rules.put(EXPERIENCE, new JsonCompare.LowerRule());
//        JsonObject result = JsonCompare.compare(rules, first, second);
//        assertEquals(second, result);
//    }
//
//    @Test
//    public void simpleCompareSecond() {
//        JsonObject first = TestUtils.json(TestUtils.Recipes.SMELTING, j -> j.addProperty(EXPERIENCE, 0.05));
//        JsonObject second = TestUtils.json(TestUtils.Recipes.SMELTING); // 0.1 experience
//
//        Map<String, JsonCompare.Rule> rules = new LinkedHashMap<>();
//        rules.put(EXPERIENCE, new JsonCompare.LowerRule());
//        JsonObject result = JsonCompare.compare(rules, first, second);
//        assertEquals(first, result);
//    }
//
//    @Test
//    public void compareHigherWins() {
//        JsonObject first = TestUtils.json(TestUtils.Recipes.SMELTING, j -> j.addProperty(EXPERIENCE, 0.05));
//        JsonObject second = TestUtils.json(TestUtils.Recipes.SMELTING); // 0.1 experience
//
//        Map<String, JsonCompare.Rule> rules = new LinkedHashMap<>();
//        rules.put(EXPERIENCE, new JsonCompare.HigherRule());
//        JsonObject result = JsonCompare.compare(rules, first, second);
//        assertEquals(second, result);
//    }
//
//    @Test
//    public void compareMulti() {
//        JsonObject a = TestUtils.json(TestUtils.Recipes.SMELTING, j -> {
//            j.addProperty(EXPERIENCE, 0.1);
//            j.addProperty(COOKING_TIME, 100);
//        });
//        JsonObject b = TestUtils.json(TestUtils.Recipes.SMELTING, j -> j.addProperty(EXPERIENCE, 0.1));
//        JsonObject c = TestUtils.json(TestUtils.Recipes.SMELTING, j -> {
//            j.addProperty(EXPERIENCE, 0.1);
//            j.addProperty(COOKING_TIME, 50);
//        });
//        JsonObject d = TestUtils.json(TestUtils.Recipes.SMELTING, j -> j.addProperty(EXPERIENCE, 0.2));
//        JsonObject e = TestUtils.json(TestUtils.Recipes.SMELTING, j -> j.addProperty(EXPERIENCE, 0.2));
//        JsonObject f = TestUtils.json(TestUtils.Recipes.SMELTING, j -> j.addProperty(EXPERIENCE, 0.1));
//        JsonObject g = TestUtils.json(TestUtils.Recipes.SMELTING, j -> {
//            j.addProperty(EXPERIENCE, 0.2);
//            j.addProperty(COOKING_TIME, 100);
//        });
//
//        Map<String, JsonCompare.Rule> rules = new LinkedHashMap<>();
//        rules.put(EXPERIENCE, new JsonCompare.HigherRule());
//        rules.put(COOKING_TIME, new JsonCompare.LowerRule());
//
//        List<JsonObject> list = Arrays.asList(a, b, c, d, e, f, g);
//        list.sort((first, second) -> JsonCompare.compare(first, second, rules));
//        List<JsonObject> results = Arrays.asList(g, d, e, c, a, b, f);
//        for (int i = 0; i < list.size(); i++) {
//            assertEquals(results.get(i), list.get(i), "Failed at index " + i);
//        }
//    }
//
//    @Test
//    public void simpleMatch() {
//        JsonObject first = TestUtils.json(TestUtils.Recipes.SMELTING);
//        JsonObject second = TestUtils.json(TestUtils.Recipes.SMELTING);
//        boolean matches = JsonCompare.matches(first, second, TestUtils.DEFAULT_COMPARE_SETTINGS);
//        assertTrue(matches);
//    }
//
//    @Test
//    public void noMatch() {
//        JsonObject first = TestUtils.json(TestUtils.Recipes.SMELTING, j -> j.addProperty(EXPERIENCE, 100));
//        JsonObject second = TestUtils.json(TestUtils.Recipes.SMELTING);
//        boolean matches = JsonCompare.matches(first, second, new JsonCompare.CompareSettings());
//        assertFalse(matches);
//    }
//
//    @Test
//    public void matchBecauseIgnore() {
//        JsonObject first = TestUtils.json(TestUtils.Recipes.SMELTING, j -> j.addProperty(EXPERIENCE, 100));
//        JsonObject second = TestUtils.json(TestUtils.Recipes.SMELTING);
//        var compareSettings = TestUtils.getDefaultCompareSettings();
//        compareSettings.ignoreField(EXPERIENCE);
//        boolean matches = JsonCompare.matches(first, second, compareSettings);
//        assertTrue(matches);
//    }
//
//    @Test
//    public void shapedNoMatch() {
//        JsonObject first = TestUtils.json(TestUtils.Recipes.SHAPED_NO_MATCH_1);
//        JsonObject second = TestUtils.json(TestUtils.Recipes.SHAPED_NO_MATCH_2);
//        JsonObject result = JsonCompare.compareShaped(first, second, TestUtils.DEFAULT_SHAPED_COMPARE_SETTINGS);
//        assertNull(result);
//    }
//
//    @Test
//    public void shapedSpecialMatch() {
//        JsonObject first = TestUtils.json(TestUtils.Recipes.SHAPED_SPECIAL_MATCH_1);
//        JsonObject second = TestUtils.json(TestUtils.Recipes.SHAPED_SPECIAL_MATCH_2);
//        JsonObject result = JsonCompare.compareShaped(first, second, TestUtils.DEFAULT_SHAPED_COMPARE_SETTINGS);
//        assertEquals(first, result);
//    }
//
//    @Test
//    public void sanitizeImplicitCount() {
//        JsonObject first = TestUtils.json(TestUtils.Recipes.SHAPED_SANITIZE_1);
//        JsonObject second = TestUtils.json(TestUtils.Recipes.SHAPED_SANITIZE_2);
//        var compareSettings = TestUtils.getDefaultShapedCompareSettings();
//        compareSettings.setShouldSanitize(true);
//        JsonObject result = JsonCompare.compareShaped(first, second, compareSettings);
//        assertEquals(first, result);
//    }
//
//    @Test
//    public void sanitizeImplicitCountNested() {
//        JsonObject first = TestUtils.json(TestUtils.Recipes.CRUSHING_NESTED_SANITIZE_1);
//        JsonObject second = TestUtils.json(TestUtils.Recipes.CRUSHING_NESTED_SANITIZE_2);
//        var compareSettings = TestUtils.getDefaultCompareSettings();
//        compareSettings.setShouldSanitize(true);
//        boolean result = JsonCompare.matches(first, second, compareSettings);
//        assertTrue(result);
//    }
}
