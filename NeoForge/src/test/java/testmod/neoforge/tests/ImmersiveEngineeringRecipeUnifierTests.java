package testmod.neoforge.tests;

import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.compat.ImmersiveEngineeringRecipeUnifier;
import testmod.gametest_core.SimpleGameTest;

import static testmod.TestUtils.assertUnify;

public class ImmersiveEngineeringRecipeUnifierTests {
    public static final RecipeUnifier UNIFIER = new ImmersiveEngineeringRecipeUnifier();

    @SimpleGameTest
    public void testAlloy() {
        assertUnify(UNIFIER, """
                {
                    "type": "immersiveengineering:alloy",
                    "input0": {
                        "item": "minecraft:test_item"
                    },
                    "input1": {
                        "item": "minecraft:test_item"
                    },
                    "result": {
                        "base_ingredient": {
                            "tag": "testmod:test_tag"
                        },
                        "count": 2
                    },
                    "time": 200
                }
                """, """
                {
                    "type": "immersiveengineering:alloy",
                    "input0": {
                        "tag": "testmod:test_tag"
                    },
                    "input1": {
                        "tag": "testmod:test_tag"
                    },
                    "result": {
                        "base_ingredient": {
                            "item": "testmod:test_item"
                        },
                        "count": 2
                    },
                    "time": 200
                }
                """);
    }

    @SimpleGameTest
    public void testRafinery() {
        assertUnify(UNIFIER, """
                {
                    "type": "immersiveengineering:refinery",
                    "catalyst": {
                        "item": "minecraft:test_item"
                    },
                    "energy": 80,
                    "input0": {
                        "amount": 8,
                        "item": "minecraft:test_item"
                    },
                    "input1": {
                        "amount": 8,
                        "item": "minecraft:test_item"
                    },
                    "result": {
                        "amount": 16,
                        "fluid": "immersiveengineering:biodiesel"
                    }
                }
                """, """
                {
                    "type": "immersiveengineering:refinery",
                    "catalyst": {
                        "tag": "testmod:test_tag"
                    },
                    "energy": 80,
                    "input0": {
                        "amount": 8,
                        "tag": "testmod:test_tag"
                    },
                    "input1": {
                        "amount": 8,
                        "tag": "testmod:test_tag"
                    },
                    "result": {
                        "amount": 16,
                        "fluid": "immersiveengineering:biodiesel"
                    }
                }
                """);
    }

    @SimpleGameTest
    public void testArcFurnace() {
        assertUnify(UNIFIER, """
                {
                    "type": "immersiveengineering:arc_furnace",
                    "additives": [
                        {
                            "item": "testmod:test_item"
                        }
                    ],
                    "energy": 51200,
                    "input": {
                        "base_ingredient": {
                            "item": "minecraft:test_item"
                        },
                        "count": 2
                    },
                    "results": [
                        {
                            "base_ingredient": {
                                "tag": "testmod:test_tag"
                            },
                            "count": 3
                        }
                    ],
                    "time": 100
                }
                """, """
                {
                    "type": "immersiveengineering:arc_furnace",
                    "additives": [
                        {
                            "tag": "testmod:test_tag"
                        }
                    ],
                    "energy": 51200,
                    "input": {
                        "base_ingredient": {
                            "tag": "testmod:test_tag"
                        },
                        "count": 2
                    },
                    "results": [
                        {
                            "base_ingredient": {
                                "item": "testmod:test_item"
                            },
                            "count": 3
                        }
                    ],
                    "time": 100
                }
                """);
    }

    @SimpleGameTest
    public void testArcFurnaceSecondaries() {
        assertUnify(UNIFIER, """
                {
                    "type": "immersiveengineering:arc_furnace",
                    "additives": [],
                    "energy": 230400,
                    "input": {
                        "item": "testmod:test_item"
                    },
                    "results": [
                        {
                            "base_ingredient": {
                                "tag": "testmod:test_tag"
                            },
                            "count": 13
                        }
                    ],
                    "secondaries": [
                        {
                            "chance": 0.5,
                            "output": {
                                "tag": "testmod:test_tag"
                            }
                        }
                    ],
                    "time": 900
                }
                """, """
                {
                    "type": "immersiveengineering:arc_furnace",
                    "additives": [],
                    "energy": 230400,
                    "input": {
                        "tag": "testmod:test_tag"
                    },
                    "results": [
                        {
                            "base_ingredient": {
                                "item": "testmod:test_item"
                            },
                            "count": 13
                        }
                    ],
                    "secondaries": [
                        {
                            "chance": 0.5,
                            "output": {
                                "item": "testmod:test_item"
                            }
                        }
                    ],
                    "time": 900
                }
                """);
    }

    @SimpleGameTest
    public void testSawmill() {
        assertUnify(UNIFIER, """
                {
                    "type": "immersiveengineering:sawmill",
                    "energy": 800,
                    "input": {
                        "item": "minecraft:test_item"
                    },
                    "result": {
                        "count": 2,
                        "item": "minecraft:test_item"
                    },
                    "secondaries": [
                        {
                            "output": {
                                "tag": "testmod:test_tag"
                            },
                            "stripping": false
                        }
                    ]
                }
                """, """
                {
                    "type": "immersiveengineering:sawmill",
                    "energy": 800,
                    "input": {
                        "tag": "testmod:test_tag"
                    },
                    "result": {
                        "count": 2,
                        "item": "testmod:test_item"
                    },
                    "secondaries": [
                        {
                            "output": {
                                "item": "testmod:test_item"
                            },
                            "stripping": false
                        }
                    ]
                }
                """);
    }

    @SimpleGameTest
    public void testSqueezer() {
        assertUnify(UNIFIER, """
                {
                    "type": "immersiveengineering:squeezer",
                    "energy": 6400,
                    "fluid": {
                        "amount": 60,
                        "fluid": "immersiveengineering:plantoil"
                    },
                    "input": {
                        "item": "minecraft:test_item"
                    }
                }
                """, """
                {
                    "type": "immersiveengineering:squeezer",
                    "energy": 6400,
                    "fluid": {
                        "amount": 60,
                        "fluid": "immersiveengineering:plantoil"
                    },
                    "input": {
                        "tag": "testmod:test_tag"
                    }
                }
                """);
    }

    @SimpleGameTest
    public void testFertilizer() {
        assertUnify(UNIFIER, """
                {
                    "type": "immersiveengineering:fertilizer",
                    "growthModifier": 1.25,
                    "input": {
                        "item": "minecraft:test_item"
                    }
                }
                """, """
                {
                    "type": "immersiveengineering:fertilizer",
                    "growthModifier": 1.25,
                    "input": {
                        "tag": "testmod:test_tag"
                    }
                }
                """);
    }

    @SimpleGameTest
    public void testMetalPress() {
        assertUnify(UNIFIER, """
                {
                    "type": "immersiveengineering:metal_press",
                    "energy": 3200,
                    "input": {
                        "base_ingredient": {
                            "item": "minecraft:test_item"
                        },
                        "count": 5
                    },
                    "mold": "immersiveengineering:mold_rod",
                    "result": {
                        "item": "minecraft:test_item"
                    }
                }
                """, """
                {
                    "type": "immersiveengineering:metal_press",
                    "energy": 3200,
                    "input": {
                        "base_ingredient": {
                            "tag": "testmod:test_tag"
                        },
                        "count": 5
                    },
                    "mold": "immersiveengineering:mold_rod",
                    "result": {
                        "item": "testmod:test_item"
                    }
                }
                """);
    }

    @SimpleGameTest
    public void testCokeOven() {
        assertUnify(UNIFIER, """
                {
                    "type": "immersiveengineering:coke_oven",
                    "creosote": 250,
                    "input": {
                        "item": "minecraft:test_item"
                    },
                    "result": {
                        "item": "minecraft:test_item"
                    },
                    "time": 900
                }
                """, """
                {
                    "type": "immersiveengineering:coke_oven",
                    "creosote": 250,
                    "input": {
                        "tag": "testmod:test_tag"
                    },
                    "result": {
                        "item": "testmod:test_item"
                    },
                    "time": 900
                }
                """);
    }
}
