package com.rinko1231.randomspawn;

import java.util.List;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Button;

public class AreaSelectionScreen extends Screen {
    //private final List<RandomSpawnConfig.AreaConfig> zones;

    public AreaSelectionScreen() {
        super(Component.literal("Choose spawn area"));
        //this.zones = zones;
    }

    @Override
    protected void init() {
        int y = 40;
        //for (RandomSpawnConfig.AreaConfig zone : zones) {
            this.addRenderableWidget(
            Button.builder(Component.literal("Zon 1"), btn -> selectZone()).bounds(this.width / 2 - 75, y, 150, 20).build()    
            );
        //}
    }

    private void selectZone() {
        this.minecraft.setScreen(null);
    }
}
