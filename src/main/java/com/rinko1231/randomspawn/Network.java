package com.rinko1231.randomspawn;

import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraft.resources.ResourceLocation;

public class Network {
    private  static final String PROTOCOL_VERSION = "1";
    private  static final ResourceLocation CHANNEL_ID = new ResourceLocation("randomspawn", "main");
    
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(CHANNEL_ID, ()->PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    
    public static void register() {
        CHANNEL.registerMessage(0, OpenGuiPacket.class, OpenGuiPacket::encode, OpenGuiPacket::decode, OpenGuiPacket::handle);
    }
}
