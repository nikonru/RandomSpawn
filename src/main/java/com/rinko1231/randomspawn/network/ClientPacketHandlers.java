package com.rinko1231.randomspawn.network;

import com.rinko1231.randomspawn.gui.AreaSelectionScreen;

import net.minecraft.client.Minecraft;

public class ClientPacketHandlers {
    public static void handleOpenGui(OpenGuiPacket msg) {
        Minecraft.getInstance().setScreen(new AreaSelectionScreen(msg.areas, msg.gameTypeId));
    }
}
