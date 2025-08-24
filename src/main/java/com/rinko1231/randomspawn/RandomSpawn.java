package com.rinko1231.randomspawn;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import com.rinko1231.randomspawn.config.RandomSpawnConfig;
import com.rinko1231.randomspawn.config.RandomSpawnConfig.AreaConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

@Mod("randomspawn")
public class RandomSpawn {

    public RandomSpawn() {
        MinecraftForge.EVENT_BUS.register(this);
        Network.register();
    }

      @SubscribeEvent(priority = EventPriority.HIGHEST)
      public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {

        if (event.getEntity() instanceof ServerPlayer player) {

            Network.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenGuiPacket());

            Level world = player.level();

            CompoundTag playerData = player.getPersistentData();
            CompoundTag data;
            if (!playerData.contains(Player.PERSISTED_NBT_TAG)) {
                data = new CompoundTag();
            }
            else {
                data = playerData.getCompound(Player.PERSISTED_NBT_TAG);
            }

            RandomSpawnConfig.load();

            if (!data.getBoolean("randomspawn:old") || RandomSpawnConfig.RandomSpawnOnEachLogin) {
                int MAX_ATTEMPTS = RandomSpawnConfig.MaxTries;

                Random random = new Random();
                AreaConfig AREA = RandomSpawnConfig.areas.get(random.nextInt(RandomSpawnConfig.areas.size()));
                int RADIUS = AREA.radius;
 
                playerData.put(Player.PERSISTED_NBT_TAG, data);

                for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++)
                {
                    int x = AREA.x + random.nextInt(RADIUS * 2) - RADIUS;
                    int z = AREA.z + random.nextInt(RADIUS * 2) - RADIUS;

                    if (AREA.shape == "square"){
                        x = AREA.x + random.nextInt(RADIUS) * randomNegation(random);
                        z = AREA.z + random.nextInt(RADIUS) * randomNegation(random);
                    }
                    else if(AREA.shape == "circle"){
                        double angle = random.nextDouble() * 2 * Math.PI; 
                        int radius = random.nextInt(RADIUS);
                        x = AREA.x + (int)(radius * Math.cos(angle));
                        z = AREA.z + (int)(radius * Math.sin(angle));
                    }
                    else{
                        System.err.println(String.format("Warning: unknown area shape: '%s' using square shape instead", AREA.shape));
                        x = AREA.x + random.nextInt(RADIUS) * randomNegation(random);
                        z = AREA.z + random.nextInt(RADIUS) * randomNegation(random);
                    }
                    //TODO: exclude failed positions
                    BlockPos teleportPos = getSafePosition(world, x, z);

                    if (teleportPos != null) {
                        player.setRespawnPosition(world.dimension(), teleportPos, 0.0f, true, false);

                        player.teleportTo(teleportPos.getX() + 0.5, teleportPos.getY() + 1, teleportPos.getZ() + 0.5);
                        player.sendSystemMessage(Component.translatable("info.randomspawn.system.success"));

                        data.putBoolean("randomspawn:old", true);
                        return;
                        }
                }

                player.sendSystemMessage(Component.translatable("info.randomspawn.system.failed"));
                data.putBoolean("randomspawn:old", true);
            }
        }
    }

    private static int randomNegation(Random random){
        if (random.nextInt(2) == 0) {
            return -1;
        } else {
            return 1;
        }
    }

    private static BlockPos getSafePosition(Level world, int x, int z) {

        BlockPos currentPos = new BlockPos(x, 0, z);
        world.getChunkAt(currentPos);
        BlockPos hmPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, currentPos);

            BlockPos pos = new BlockPos(x, hmPos.getY(), z);
            BlockPos PosPlus = new BlockPos(x, hmPos.getY()+1, z);  // position above the block
            BlockPos PosPlusPlus = new BlockPos(x, hmPos.getY()+2, z);

            AtomicReference<String> biomeIdRef = new AtomicReference<>(null);

            world.getBiome(pos).unwrap()
                    .ifLeft(r -> biomeIdRef.set(r.location().toString()))
                    .ifRight(b -> biomeIdRef.set(ForgeRegistries.BIOMES.getKey(b).toString()));

            String biomeId = biomeIdRef.get();
            boolean goodBiome = false;
            if (biomeId != null)
               goodBiome = !RandomSpawnConfig.biomeBlacklist.contains(biomeId);

            if (world.getWorldBorder().isWithinBounds(pos) && goodBiome
             && !world.getBiome(pos).is(BiomeTags.IS_OCEAN)
             && !world.getBiome(pos).is(BiomeTags.IS_RIVER))
               {
                   Block block = world.getBlockState(pos).getBlock();
                   String blockId = ForgeRegistries.BLOCKS.getKey(block).toString();
                   boolean goodBlock = !RandomSpawnConfig.blockBlacklist.contains(blockId);

               // ensuring there is enough space above
               if (!world.getBlockState(pos).isAir() && goodBlock
                 && world.getBlockState(PosPlus).isAir()
                 && world.getBlockState(PosPlusPlus).isAir())
                 {
                     return PosPlus;}
               }

        return null;
    }
}
