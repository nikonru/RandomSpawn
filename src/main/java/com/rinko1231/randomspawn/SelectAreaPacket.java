package com.rinko1231.randomspawn;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;


public class SelectAreaPacket {
    public final int areaId;

    public SelectAreaPacket(int areaId) {
        this.areaId = areaId;
    }

    public static void encode(SelectAreaPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.areaId);
    }

    public static SelectAreaPacket decode(FriendlyByteBuf buf) {
        return new SelectAreaPacket(buf.readInt());
    }

    public static void handle(SelectAreaPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player != null) {
                RandomSpawn.setRandomSpawn(player , msg.areaId);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
