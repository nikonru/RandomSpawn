package com.rinko1231.randomspawn;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;

public class AreaSelectionScreen extends Screen {
    private static final ResourceLocation DIRT_TEXTURE = new ResourceLocation("minecraft", "textures/block/dirt.png");

    private final List<String> areas;

    public AreaSelectionScreen(List<String> areas) {
        super(Component.translatable("info.randomspawn.gui.choose_spawn"));
        this.areas = new ArrayList<>(areas);
    }

    @Override
    protected void init() {
        int y = 40;

        for (int i = 0; i < areas.size(); i++) {
            String area = areas.get(i);
            final int areaId = i;
            this.addRenderableWidget(
                Button.builder(Component.literal(area), btn -> selectZone(areaId)).bounds(this.width / 2 - 75, y, 150, 20).build()    
            );
            y += 25;
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderDirtBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private void selectZone(int areaId) {
        Network.CHANNEL.sendToServer(new SelectAreaPacket(areaId));

        this.minecraft.setScreen(null);
    }
}
