package com.rinko1231.randomspawn.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.Tesselator;
import com.rinko1231.randomspawn.network.Network;
import com.rinko1231.randomspawn.network.SelectAreaPacket;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ScrollPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;

public class AreaSelectionScreen extends Screen {
    private final List<String> areas;
    private final int gameTypeId;
    private AreaScrollPanel scrollPanel;

    public AreaSelectionScreen(List<String> areas, int gameTypeId) {
        super(Component.translatable("info.randomspawn.gui.choose_spawn"));
        this.areas = new ArrayList<>(areas);
        this.gameTypeId = gameTypeId;
    }

    @Override
    protected void init() {
        int top = 40;
        int bottom = this.height - 30;
        int left = this.width / 2 - 100;
        int right = this.width / 2 + 100;

        scrollPanel = new AreaScrollPanel(minecraft, right - left, bottom - top, top, left, areas, gameTypeId);
        addRenderableWidget(scrollPanel);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderDirtBackground(pGuiGraphics);
        pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private static class AreaScrollPanel extends ScrollPanel {
        private final List<Button> buttons = new ArrayList<>();
        private final List<String> areas;
        private final int gameTypeId;
        private final Minecraft minecraft;

        public AreaScrollPanel(Minecraft mc, int width, int height, int top, int left, List<String> areas, int gameTypeId) {
            super(mc, width, height, top, left);
            this.minecraft = mc;
            this.areas = areas;
            this.gameTypeId = gameTypeId;

            int y = 0;
            for (int i = 0; i < areas.size(); i++) {
                String area = areas.get(i);
                final int id = i;
                Button btn = Button.builder(Component.literal(area),
                        b -> selectZone(id)).bounds(left + 10, top + y, width - 20, 20).build();
                buttons.add(btn);
                y += 25;
            }
        }

        @Override
        protected int getContentHeight() {
            return areas.size() * 25;
        }

        @Override
        protected void drawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
            int y = relativeY - top;
            for (Button btn : buttons) {
                btn.setY(this.top + y + 5);
                btn.setX(this.left + 10);
                btn.render(guiGraphics, mouseX, mouseY, 0);
                y += 25;
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            for (Button btn : buttons) {
                if (btn.isMouseOver(mouseX, mouseY)) {
                    btn.onPress();
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(NarrationElementOutput output) {
        }

        private void selectZone(int areaId) {
            Network.CHANNEL.sendToServer(new SelectAreaPacket(areaId, gameTypeId));

            this.minecraft.setScreen(null);
        }
    }
}
