package com.rinko1231.randomspawn;

import java.util.function.Supplier;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class OpenGuiPacket {
    public final int gameTypeId;
    public final List<String> areas;

    public OpenGuiPacket(List<String> areas, int gameType) {
        this.gameTypeId = gameType;
        this.areas = new ArrayList<>(areas);;
    }

    public static void encode(OpenGuiPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.gameTypeId);
        buf.writeInt(msg.areas.size());
        for (int i = 0; i < msg.areas.size(); i++) {
            buf.writeUtf(msg.areas.get(i));
        }
    }
    public static OpenGuiPacket decode( FriendlyByteBuf buf) {
        List<String> areas_ = new ArrayList<>();

        int gameType = buf.readInt();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            areas_.add(buf.readUtf());
        }
        return new OpenGuiPacket(areas_, gameType);
    }

    public static void handle(OpenGuiPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientPacketHandlers.handleOpenGui(msg);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
