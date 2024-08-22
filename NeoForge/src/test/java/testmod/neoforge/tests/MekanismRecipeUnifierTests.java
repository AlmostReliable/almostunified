package testmod.neoforge.tests;

import com.almostreliable.unified.api.unification.recipe.RecipeUnifier;
import com.almostreliable.unified.compat.unification.MekanismRecipeUnifier;
import testmod.gametest_core.SimpleGameTest;

import static testmod.TestUtils.assertNoUnify;
import static testmod.TestUtils.assertUnify;

public class MekanismRecipeUnifierTests {

    public static final RecipeUnifier UNIFIER = new MekanismRecipeUnifier();

    @SimpleGameTest
    public void testMainInputAndExtraInput() {
        assertUnify(UNIFIER, """
                  {
                  "type": "mekanism:combining",
                  "extraInput": {
                    "ingredient": {
                      "item": "minecraft:test_item"
                    }
                  },
                  "mainInput": {
                    "ingredient": {
                      "item": "minecraft:test_item"
                    }
                  },
                  "output": {
                    "item": "minecraft:gravel"
                  }
                }
                """, """
                {
                  "type": "mekanism:combining",
                  "extraInput": {
                    "ingredient": {
                      "tag": "testmod:test_tag"
                    }
                  },
                  "mainInput": {
                    "ingredient": {
                      "tag": "testmod:test_tag"
                    }
                  },
                  "output": {
                    "item": "minecraft:gravel"
                  }
                }
                """);
    }

    @SimpleGameTest
    public void testMainInputAndExtraInput_Noop() {
        assertNoUnify(UNIFIER, """
                  {
                  "type": "mekanism:combining",
                  "extraInput": {
                    "ingredient": {
                      "item": "minecraft:nether_star"
                    }
                  },
                  "mainInput": {
                    "ingredient": {
                      "item": "minecraft:stick"
                    }
                  },
                  "output": {
                    "item": "minecraft:gravel"
                  }
                }
                """);
    }

    @SimpleGameTest
    public void testItemInput_PaintingType() {
        assertUnify(UNIFIER, """
                {
                  "type": "mekanism:painting",
                  "chemicalInput": {
                    "amount": 256,
                    "pigment": "mekanism:gray"
                  },
                  "itemInput": {
                    "ingredient": [
                      {
                        "item": "minecraft:test_item"
                      },
                      {
                        "item": "testmod:test_item"
                      },
                      {
                        "item": "minecraft:stick"
                      }
                    ]
                  },
                  "output": {
                    "item": "ilikewood:biomesoplenty_gray_cherry_bed"
                  }
                }
                """, """
                {
                  "type": "mekanism:painting",
                  "chemicalInput": {
                    "amount": 256,
                    "pigment": "mekanism:gray"
                  },
                  "itemInput": {
                    "ingredient": [
                      {
                        "tag": "testmod:test_tag"
                      },
                      {
                        "tag": "testmod:test_tag"
                      },
                      {
                        "item": "minecraft:stick"
                      }
                    ]
                  },
                  "output": {
                    "item": "ilikewood:biomesoplenty_gray_cherry_bed"
                  }
                }
                """);
    }

    @SimpleGameTest
    public void testItemInput_PaintingType_Noop() {
        assertNoUnify(UNIFIER, """
                {
                  "type": "mekanism:painting",
                  "chemicalInput": {
                    "amount": 256,
                    "pigment": "mekanism:gray"
                  },
                  "itemInput": {
                    "ingredient": [
                      {
                        "item": "minecraft:apple"
                      },
                      {
                        "item": "testmod:invalid_item"
                      },
                      {
                        "item": "minecraft:stick"
                      }
                    ]
                  },
                  "output": {
                    "item": "ilikewood:biomesoplenty_gray_cherry_bed"
                  }
                }
                """);
    }

    @SimpleGameTest
    public void testItemInput_MetallurgicInfusingType() {
        assertUnify(UNIFIER, """
                {
                  "type": "mekanism:metallurgic_infusing",
                  "chemicalInput": {
                    "amount": 10,
                    "tag": "mekanism:bio"
                  },
                  "itemInput": {
                    "ingredient": {
                      "item": "minecraft:test_item"
                    }
                  },
                  "output": {
                    "item": "byg:mossy_stone_slab"
                  }
                }
                """, """
                {
                  "type": "mekanism:metallurgic_infusing",
                  "chemicalInput": {
                    "amount": 10,
                    "tag": "mekanism:bio"
                  },
                  "itemInput": {
                    "ingredient": {
                      "tag": "testmod:test_tag"
                    }
                  },
                  "output": {
                    "item": "byg:mossy_stone_slab"
                  }
                }
                """);
    }

    @SimpleGameTest
    public void testItemInput_MetallurgicInfusingType_Noop() {
        assertNoUnify(UNIFIER, """
                {
                  "type": "mekanism:metallurgic_infusing",
                  "chemicalInput": {
                    "amount": 10,
                    "tag": "mekanism:bio"
                  },
                  "itemInput": {
                    "ingredient": {
                      "tag": "testmod:test_tag"
                    }
                  },
                  "output": {
                    "item": "byg:mossy_stone_slab"
                  }
                }
                """);
    }

    @SimpleGameTest
    public void testMainOutputSecondaryOutput() {
        assertUnify(UNIFIER, """
                {
                  "type": "mekanism:sawing",
                  "input": {
                    "ingredient": {
                      "item": "testmod:test_item"
                    }
                  },
                  "mainOutput": {
                    "count": 3,
                    "item": "minecraft:test_item"
                  },
                  "secondaryChance": 1.0,
                  "secondaryOutput": {
                    "count": 3,
                    "item": "minecraft:test_item"
                  }
                }
                """, """
                {
                  "type": "mekanism:sawing",
                  "input": {
                    "ingredient": {
                      "tag": "testmod:test_tag"
                    }
                  },
                  "mainOutput": {
                    "count": 3,
                    "item": "testmod:test_item"
                  },
                  "secondaryChance": 1.0,
                  "secondaryOutput": {
                    "count": 3,
                    "item": "testmod:test_item"
                  }
                }
                """);
    }

    @SimpleGameTest
    public void testItemInputItemOutput() {
        assertUnify(UNIFIER, """
                {
                  "type": "mekanism:reaction",
                  "duration": 100,
                  "fluidInput": {
                    "amount": 1000,
                    "tag": "minecraft:water"
                  },
                  "gasInput": {
                    "amount": 1000,
                    "gas": "mekanism:plutonium"
                  },
                  "gasOutput": {
                    "amount": 1000,
                    "gas": "mekanism:spent_nuclear_waste"
                  },
                  "itemInput": {
                    "ingredient": {
                      "item": "minecraft:test_item"
                    }
                  },
                  "itemOutput": {
                    "item": "minecraft:test_item"
                  }
                }
                """, """
                {
                  "type": "mekanism:reaction",
                  "duration": 100,
                  "fluidInput": {
                    "amount": 1000,
                    "tag": "minecraft:water"
                  },
                  "gasInput": {
                    "amount": 1000,
                    "gas": "mekanism:plutonium"
                  },
                  "gasOutput": {
                    "amount": 1000,
                    "gas": "mekanism:spent_nuclear_waste"
                  },
                  "itemInput": {
                    "ingredient": {
                      "tag": "testmod:test_tag"
                    }
                  },
                  "itemOutput": {
                    "item": "testmod:test_item"
                  }
                }
                """);
    }
}
