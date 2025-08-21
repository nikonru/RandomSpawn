package com.rinko1231.randomspawn.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RandomSpawnConfig {
    public static int MaxTries = 10;
    public static List<String> biomeBlacklist = List.of("mod1:biome2", "mod3:biome4");
    public static List<String> blockBlacklist = List.of("minecraft:magma_block", "minecraft:cactus", "minecraft:lava");

    public static class AreaConfig {
        public String name;
        public String shape;
        public int radius;
        
        public int x;
        public int z;

        public AreaConfig(String name, String shape, int radius, int x, int z) {
            this.name = name;
            this.shape = shape;
            this.radius = radius;
            this.x = x;
            this.z = z;
        }
    }

    public static List<AreaConfig> areas = new ArrayList<>();

    private static AreaConfig defaultArea = new AreaConfig("default", "square", 100, 0, 0);

    public static void load() {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve("RandomSpawn.toml");

        boolean success = false;

        if (configPath.toFile().exists()) {
            try (CommentedFileConfig config = CommentedFileConfig.builder(configPath)
                    .writingMode(WritingMode.REPLACE)
                    .build()) {

                config.load();

                MaxTries = config.getOrElse("Config.MaxTries", MaxTries);
                biomeBlacklist = config.getOrElse("Config.biome Blacklist", biomeBlacklist);
                blockBlacklist = config.getOrElse("Config.block Blacklist", blockBlacklist);

                areas.clear();
                
                boolean corruptedArea = false;

                List<?> rawAreas = config.getOrElse("areas", new ArrayList<>());
                for (Object obj : rawAreas) {
                        if (!(obj instanceof CommentedConfig)) {
                                corruptedArea = true;
                                break;
                        }

                        CommentedConfig raw = (CommentedConfig) obj;

                        String name = raw.getOrElse("name", "default");
                        String shape = raw.getOrElse("shape", "square");
                        int radius = raw.getOrElse("radius", 100);

                        int x = raw.getOrElse("x", 0);
                        int z = raw.getOrElse("z", 0);

                        areas.add(new AreaConfig(name, shape, radius, x, z));
                }

                success = true && !corruptedArea;

            } catch (Exception e) {
                System.err.println("Failed to load RandomSpawn.toml, recreating defaults: " + e);
            }
        }

        if (!success) {
            areas.clear();
            areas.add(defaultArea);

            try (CommentedFileConfig config = CommentedFileConfig.builder(configPath)
                    .writingMode(WritingMode.REPLACE)
                    .build()) {

                config.set("Config.MaxTries", MaxTries);
                config.set("Config.biome Blacklist", new ArrayList<>(biomeBlacklist));
                config.set("Config.block Blacklist", new ArrayList<>(blockBlacklist));

                CommentedConfig area = CommentedConfig.inMemory();
                area.set("name", defaultArea.name);
                area.set("shape", defaultArea.shape);
                area.set("radius", defaultArea.shape);

                area.set("x", defaultArea.x);
                area.set("z", defaultArea.z);

                List<CommentedConfig> areaList = List.of(area);
                config.set("areas", areaList); 

                config.save();
            } catch (Exception e) {
                System.err.println("Failed to create default RandomSpawn.toml: " + e);
            }
        }
    }
}
