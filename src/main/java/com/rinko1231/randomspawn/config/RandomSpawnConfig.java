package com.rinko1231.randomspawn.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;


public class RandomSpawnConfig
{
    private static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec.IntValue MaxDistance;
    public static ForgeConfigSpec.IntValue MaxTries;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> biomeBlacklist;

    static
    {
        BUILDER.push("Random Spawn Config");
        MaxDistance = BUILDER
                .comment("Max RTP Distance")
                .defineInRange("MaxDistance", 500, 1, 10000);
        MaxTries = BUILDER
                .comment("Max RTP Tries")
                .defineInRange("MaxTries", 5, 1, 20);
        biomeBlacklist = BUILDER
                .comment("UNUSED: Biome Blacklist")
                .defineList("biome Blacklist", List.of("minecraft:ocean"),
                        element -> element instanceof String);
        SPEC = BUILDER.build();
    }
    public static void setup()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "RandomSpawn.toml");
    }

}
