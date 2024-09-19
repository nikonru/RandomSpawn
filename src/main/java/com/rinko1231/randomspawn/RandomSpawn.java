package com.rinko1231.randomspawn;

import com.rinko1231.randomspawn.config.RandomSpawnConfig;
import net.minecraft.core.BlockPos;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import net.minecraft.resources.ResourceLocation;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;

import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;


@Mod("randomspawn")
public class RandomSpawn {

    // 构造函数 - 这个是模组的启动入口
    public RandomSpawn() {
        // 注册事件总线 (Event Bus)
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

            if (!data.getBoolean("teampotato:old")) {

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
                    BlockPos teleportPos = getSafePosition(world, x, z, player);

                    if (teleportPos != null) {
                        // 找到合适位置，传送玩家
                        player.teleportTo(teleportPos.getX() + 0.5, teleportPos.getY() + 1, teleportPos.getZ() + 0.5);
                        player.sendSystemMessage(Component.literal("你已被随机传送，祝你好运！"));
                        // 添加“oldPlayer”标签
                        data.putBoolean("teampotato:old", true);
                        return;
                        }
                }
                // 如果尝试后仍未找到合适的位置
                player.sendSystemMessage(Component.literal("RTP失败，无法找到安全的传送位置。"));
                data.putBoolean("teampotato:old", true);
            }
        }
    }

    // 获取安全的传送位置
    //其实没必要加个player参数，是为了方便测试发消息
    private static BlockPos getSafePosition(Level world, int x, int z, Player player) {
        for (int y = world.getSeaLevel(); y < world.getMaxBuildHeight(); y++) {

            BlockPos pos = new BlockPos(x, y, z);
            BlockPos PosPlus = new BlockPos(x, y + 1, z);  // 上方一格的位置
            BlockPos PosPlusPlus = new BlockPos(x, y + 2, z);


            String biomeId=world.getBiome(pos).toString();
            boolean goodBiome = true;

            player.sendSystemMessage(Component.literal(biomeId));  // 发送消息给玩家


            goodBiome = !RandomSpawnConfig.biomeBlacklist.get().contains(biomeId);

            if (world.getWorldBorder().isWithinBounds(pos) && goodBiome
             && !world.getBiome(pos).is(BiomeTags.IS_OCEAN)// 排除含有is_ocean标签的群系
             && !world.getBiome(pos).is(BiomeTags.IS_RIVER))
            {
            // 检查当前位置和上方一格的方块是否安全
            if (!world.getBlockState(pos).isAir()
             && world.getBlockState(pos).getBlock() != Blocks.LAVA
             && world.getBlockState(pos).getBlock() != Blocks.CACTUS
             && world.getBlockState(pos).getBlock() != Blocks.MAGMA_BLOCK
             && world.getBlockState(PosPlus).isAir()// 确保上方有足够的空间
             && world.getBlockState(PosPlusPlus).isAir()
               )
               {return pos;}
            }
        }
        // 如果没有找到合适的方块，返回null
        return null;
    }



}
