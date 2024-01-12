package testmod.old.utils;

import com.almostreliable.unified.api.TagMap;
import com.almostreliable.unified.utils.TagMapImpl;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class TagMapTests {

    public static TagMap<Item> testTagMap() {
        TagMap<Item> tagMap = new TagMapImpl<>();
        TagKey<Item> bronzeOreTag = TagKey.create(Registries.ITEM, new ResourceLocation("forge:ores/bronze"));
        TagKey<Item> invarOreTag = TagKey.create(Registries.ITEM, new ResourceLocation("forge:ores/invar"));
        TagKey<Item> tinOreTag = TagKey.create(Registries.ITEM, new ResourceLocation("forge:ores/tin"));
        TagKey<Item> silverOreTag = TagKey.create(Registries.ITEM, new ResourceLocation("forge:ores/silver"));

        // TODO Tagmap builder pls
//        tagMap.put(bronzeOreTag, TestUtils.mod1RL("bronze_ore"));
//        tagMap.put(bronzeOreTag, TestUtils.mod2RL("bronze_ore"));
//        tagMap.put(bronzeOreTag, TestUtils.mod3RL("bronze_ore"));
//
//        tagMap.put(invarOreTag, TestUtils.mod1RL("invar_ore"));
//        tagMap.put(invarOreTag, TestUtils.mod2RL("invar_ore"));
//        tagMap.put(invarOreTag, TestUtils.mod3RL("invar_ore"));
//        tagMap.put(invarOreTag, TestUtils.mod4RL("invar_ore"));
//
//        tagMap.put(tinOreTag, TestUtils.mod3RL("tin_ore"));
//        tagMap.put(tinOreTag, TestUtils.mod4RL("tin_ore"));
//
//        tagMap.put(silverOreTag, TestUtils.mod3RL("silver_ore"));
//        tagMap.put(silverOreTag, TestUtils.mod4RL("silver_ore"));
//        tagMap.put(silverOreTag, TestUtils.mod5RL("silver_ore"));
        return tagMap;
    }

//    @Test
//    public void simpleCheck() {
//        TagMap<Item> tagMap = new TagMap<>();
//        UnifyTag<Item> bronzeOreTag = UnifyTag.item(new ResourceLocation("forge:ores/bronze"));
//        tagMap.put(bronzeOreTag, TestUtils.mod1RL("bronze_ore"));
//        tagMap.put(bronzeOreTag, TestUtils.mod2RL("bronze_ore"));
//        tagMap.put(bronzeOreTag, TestUtils.mod3RL("bronze_ore"));
//        tagMap.put(bronzeOreTag, TestUtils.mod4RL("bronze_ore"));
//        tagMap.put(bronzeOreTag, TestUtils.mod5RL("bronze_ore"));
//
//        assertEquals(tagMap.getEntriesByTag(bronzeOreTag).size(), 5);
//        assertEquals(tagMap.getTagsByEntry(TestUtils.mod1RL("bronze_ore")).size(), 1);
//        assertEquals(tagMap.getTagsByEntry(TestUtils.mod2RL("bronze_ore")).size(), 1);
//        assertEquals(tagMap.getTagsByEntry(TestUtils.mod3RL("bronze_ore")).size(), 1);
//        assertEquals(tagMap.getTagsByEntry(TestUtils.mod4RL("bronze_ore")).size(), 1);
//        assertEquals(tagMap.getTagsByEntry(TestUtils.mod5RL("bronze_ore")).size(), 1);
//
//        tagMap.put(UnifyTag.item(new ResourceLocation("forge:ores/invar")), TestUtils.mod1RL("invar_ore"));
//
//        assertEquals(tagMap.tagSize(), 2);
//        assertEquals(tagMap.itemSize(), 6);
//    }
}
