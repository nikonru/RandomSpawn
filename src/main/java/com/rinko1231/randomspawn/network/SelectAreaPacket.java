package com.rinko1231.randomspawn.network;

import java.util.function.Supplier;

import com.rinko1231.randomspawn.RandomSpawn;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;


public class SelectAreaPacket {
    public final int gameTypeId;
    public final int areaId;

    public SelectAreaPacket(int areaId, int gameTypeId) {
        this.areaId = areaId;
        this.gameTypeId = gameTypeId;
    }

    public static void encode(SelectAreaPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.gameTypeId);
        buf.writeInt(msg.areaId);
    }

    public static SelectAreaPacket decode(FriendlyByteBuf buf) {
        int gameTypeId = buf.readInt();
        int area = buf.readInt();
        return new SelectAreaPacket(area, gameTypeId);
    }

    public static void handle(SelectAreaPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player != null) {
                RandomSpawn.setRandomSpawn(player , msg.areaId, msg.gameTypeId);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
