package com.rinko1231.randomspawn;

import com.rinko1231.randomspawn.config.RandomSpawnConfig;
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
import net.minecraftforge.registries.ForgeRegistries;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;


@Mod("randomspawn")
public class RandomSpawn {

    // 构造函数 - 这个是模组的启动入口
    public RandomSpawn() {
        RandomSpawnConfig.setup();
        MinecraftForge.EVENT_BUS.register(this);
    }

      @SubscribeEvent(priority = EventPriority.HIGHEST)
      public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {

        int RANGE = RandomSpawnConfig.MaxDistance.get();
        int MAX_ATTEMPTS = RandomSpawnConfig.MaxTries.get();

        if (event.getEntity() instanceof ServerPlayer player) {
            Level world = player.level();

            CompoundTag playerData = player.getPersistentData();
            CompoundTag data;
            if (!playerData.contains(Player.PERSISTED_NBT_TAG)) {
                data = new CompoundTag();
            }
            else {
                data = playerData.getCompound(Player.PERSISTED_NBT_TAG);
            }

            if (!data.getBoolean("randomspawn:old")) {

                playerData.put(Player.PERSISTED_NBT_TAG, data);

                // 获取玩家出生点的坐标
                BlockPos spawnPos = player.getRespawnPosition();
                if (spawnPos == null) {
                    // 如果没有设置出生点，使用世界默认出生点
                    spawnPos = world.getSharedSpawnPos();
                }
                // 以出生点为中心进行随机传送
                Random random = new Random();
                for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++)
                {
                    // 生成随机的偏移量
                    int x = spawnPos.getX() + random.nextInt(RANGE * 2) - RANGE;
                    int z = spawnPos.getZ() + random.nextInt(RANGE * 2) - RANGE;

                    // 获取安全的传送位置
                    BlockPos teleportPos = getSafePosition(world, x, z);

                    if (teleportPos != null) {
                        // 找到合适位置，传送玩家
                        player.teleportTo(teleportPos.getX() + 0.5, teleportPos.getY() + 1, teleportPos.getZ() + 0.5);
                        player.sendSystemMessage(Component.translatable("info.randomspawn.system.success"));
                        // 添加“oldPlayer”标签
                        data.putBoolean("randomspawn:old", true);
                        return;
                        }
                }
                // 如果尝试后仍未找到合适的位置
                player.sendSystemMessage(Component.translatable("info.randomspawn.system.failed"));
                data.putBoolean("randomspawn:old", true);
            }
        }
    }

    // 获取安全的传送位置
    private static BlockPos getSafePosition(Level world, int x, int z) {

        BlockPos currentPos = new BlockPos(x, 0, z);
        world.getChunkAt(currentPos);
        BlockPos hmPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, currentPos);

            BlockPos pos = new BlockPos(x, hmPos.getY(), z);
            BlockPos PosPlus = new BlockPos(x, hmPos.getY()+1, z);  // 上方一格的位置
            BlockPos PosPlusPlus = new BlockPos(x, hmPos.getY()+2, z);

            AtomicReference<String> biomeIdRef = new AtomicReference<>(null);

            world.getBiome(pos).unwrap()
                    .ifLeft(r -> biomeIdRef.set(r.location().toString()))
                    .ifRight(b -> biomeIdRef.set(ForgeRegistries.BIOMES.getKey(b).toString()));

            String biomeId = biomeIdRef.get();
            boolean goodBiome = false;
            if (biomeId != null)
               goodBiome = !RandomSpawnConfig.biomeBlacklist.get().contains(biomeId);

            if (world.getWorldBorder().isWithinBounds(pos) && goodBiome
             && !world.getBiome(pos).is(BiomeTags.IS_OCEAN)// 排除含有is_ocean标签的群系
             && !world.getBiome(pos).is(BiomeTags.IS_RIVER))
               {
                   Block block = world.getBlockState(pos).getBlock();
                   String blockId = ForgeRegistries.BLOCKS.getKey(block).toString();
                   boolean goodBlock = !RandomSpawnConfig.blockBlacklist.get().contains(blockId);
                   // 确保上方有足够的空间
               if (!world.getBlockState(pos).isAir() && goodBlock
                 && world.getBlockState(PosPlus).isAir()
                 && world.getBlockState(PosPlusPlus).isAir())
                 {
                     return PosPlus;}
               }

        // 如果没有找到合适的方块，返回null
        return null;
    }



}
