package com.rinko1231.randomspawn;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class OpenGuiPacket {
    public static void encode(OpenGuiPacket msg, FriendlyByteBuf buf) {}
    public static OpenGuiPacket decode(FriendlyByteBuf buf) {
        return new OpenGuiPacket();
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
