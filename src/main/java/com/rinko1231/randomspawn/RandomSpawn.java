package com.rinko1231.randomspawn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import com.rinko1231.randomspawn.config.RandomSpawnConfig;
import com.rinko1231.randomspawn.config.RandomSpawnConfig.AreaConfig;
import com.rinko1231.randomspawn.network.Network;
import com.rinko1231.randomspawn.network.OpenGuiPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
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
        RandomSpawnConfig.load();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        MinecraftServer server = event.getEntity().getServer();
        if (server == null) return;
        if (!server.isDedicatedServer()) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            CompoundTag playerData = player.getPersistentData();
            CompoundTag data;
            if (!playerData.contains(Player.PERSISTED_NBT_TAG)) {
                data = new CompoundTag();
                playerData.put(Player.PERSISTED_NBT_TAG, data);
            } else {
                data = playerData.getCompound(Player.PERSISTED_NBT_TAG);
            }
            
            if (!data.getBoolean("randomspawn:old") || RandomSpawnConfig.RandomSpawnOnEachLogin) {
                int gameTypeId = getPlayerGameType(player).getId();
                player.setGameMode(GameType.SPECTATOR); // Making player non-existent to the world until his decision about spawn area

                if (RandomSpawnConfig.RandomSpawnArea){
                    Random random = new Random();
                    setRandomSpawn(player, random.nextInt(RandomSpawnConfig.areas.size()), gameTypeId);
                } else {
                    List<String> areas = new ArrayList<>();
                    for (int i = 0; i < RandomSpawnConfig.areas.size(); i++) {
                        AreaConfig area = RandomSpawnConfig.areas.get(i);
                        areas.add(area.name);
                    }

                    Network.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenGuiPacket(areas, gameTypeId));
                }
            }
        }
    }

    public static void setRandomSpawn(ServerPlayer player, int areaId, int gameTypeId){
        GameType gameType = GameType.byId(gameTypeId);
        Level world = player.level();

        CompoundTag data = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);

        Random random = new Random();
        AreaConfig AREA = RandomSpawnConfig.areas.get(areaId);
        int RADIUS = AREA.radius;

        for (int attempt = 0; attempt < RandomSpawnConfig.MaxTries; attempt++)
        {
            int x = AREA.x + random.nextInt(RADIUS * 2) - RADIUS;
            int z = AREA.z + random.nextInt(RADIUS * 2) - RADIUS;

            if ("square".equals(AREA.shape)) {
                x = AREA.x + random.nextInt(RADIUS) * randomNegation(random);
                z = AREA.z + random.nextInt(RADIUS) * randomNegation(random);
            }
            else if("circle".equals(AREA.shape)) {
                double angle = random.nextDouble() * 2 * Math.PI; 
                int radius = random.nextInt(RADIUS);
                x = AREA.x + (int)(radius * Math.cos(angle));
                z = AREA.z + (int)(radius * Math.sin(angle));
            }
            else {
                System.err.println(String.format("Warning: unknown area shape: '%s' using square shape instead", AREA.shape));
                x = AREA.x + random.nextInt(RADIUS) * randomNegation(random);
                z = AREA.z + random.nextInt(RADIUS) * randomNegation(random);
            }

            BlockPos teleportPos = getSafePosition(world, x, z);
            
            // TODO: (possible optimization) exclude failed positions
            if (teleportPos != null) {
                player.setRespawnPosition(world.dimension(), teleportPos, 0.0f, true, false);

                player.teleportTo(teleportPos.getX() + 0.5, teleportPos.getY() + 1, teleportPos.getZ() + 0.5);
                player.sendSystemMessage(Component.translatable("info.randomspawn.system.success"));

                data.putBoolean("randomspawn:old", true);
                player.setGameMode(gameType);
                return;
            }
        }

        player.sendSystemMessage(Component.translatable("info.randomspawn.system.failed"));
        data.putBoolean("randomspawn:old", true);

        player.setGameMode(gameType);
    }

    private static GameType getPlayerGameType(ServerPlayer player){
        // NOTE: so, basically we have excluded Adventure and Spectator modes as initial modes for players
        if(player.isCreative()) {
            return GameType.CREATIVE;
        }
        return GameType.SURVIVAL;
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
