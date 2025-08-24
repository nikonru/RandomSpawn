package com.rinko1231.randomspawn;

import net.minecraft.client.Minecraft;

public class ClientPacketHandlers {
    public static void handleOpenGui(OpenGuiPacket msg) {
        Minecraft.getInstance().setScreen(new AreaSelectionScreen());
    }
}
