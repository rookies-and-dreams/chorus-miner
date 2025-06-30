package net.meteox.chorus_miner.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.meteox.chorus_miner.ChorusMiner;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ChorusMinerScreen extends AbstractContainerScreen<ChorusMinerMenu> {

    final int PROGRESS_ARROW_U_COORD = 176;
    final int PROGRESS_ARROW_V_COORD = 0;
    final int PROGRESS_ARROW_X_OFFSET = 65;
    final int PROGRESS_ARROW_Y_OFFSET = 28;
    final int PROGRESS_ARROW_WIDTH = 8;
    final int PROGRESS_ARROW_HEIGHT = 26;

    final int CHARGE_METER_U_COORD = 184;
    final int CHARGE_METER_V_COORD = 0;
    final int CHARGE_METER_MAX_U_COORD = 239;
    final int CHARGE_METER_X_OFFSET = 79;
    final int CHARGE_METER_Y_OFFSET = 22;
    final int CHARGE_METER_WIDTH = 18;
    final int CHARGE_METER_HEIGHT = 36;
    int chargeMeterCurrentUCoord = 0;

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ChorusMiner.MOD_ID, "textures/gui/chorus_miner_gui.png");

    public ChorusMinerScreen(ChorusMinerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
        this.chargeMeterCurrentUCoord = CHARGE_METER_U_COORD;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        renderProgressArrow(guiGraphics, x, y);
        renderProgressCharge(guiGraphics, x, y);
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        // Update animation
        chargeMeterCurrentUCoord++;
        if (chargeMeterCurrentUCoord >= CHARGE_METER_MAX_U_COORD)
        {
            chargeMeterCurrentUCoord = CHARGE_METER_U_COORD;
        }
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if(menu.isCrafting()) {
            guiGraphics.blit(
                TEXTURE,
                (x + PROGRESS_ARROW_X_OFFSET),
                (y + PROGRESS_ARROW_Y_OFFSET),
                PROGRESS_ARROW_U_COORD,
                PROGRESS_ARROW_V_COORD,
                PROGRESS_ARROW_WIDTH,
                menu.getScaledArrowProgress(PROGRESS_ARROW_HEIGHT)
            );
        }
    }

    private void renderProgressCharge(GuiGraphics guiGraphics, int x, int y){

        int progressYOffset = CHARGE_METER_HEIGHT - menu.getScaledChargeProgress(CHARGE_METER_HEIGHT);
        int progressVOffset = CHARGE_METER_HEIGHT - menu.getScaledChargeProgress(CHARGE_METER_HEIGHT);

        guiGraphics.blit(
            TEXTURE,
            (x + CHARGE_METER_X_OFFSET),
            (y + CHARGE_METER_Y_OFFSET + progressYOffset),
            chargeMeterCurrentUCoord,
            progressVOffset,
            CHARGE_METER_WIDTH,
            menu.getScaledChargeProgress(CHARGE_METER_HEIGHT)
        );
    }

    @Override
    public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY){
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        String tooltip = menu.getChargeProgress() + " / " + menu.getChargeMaxProgress();

        if (isHovering(CHARGE_METER_X_OFFSET, CHARGE_METER_Y_OFFSET, CHARGE_METER_WIDTH, CHARGE_METER_HEIGHT, mouseX, mouseY)) {
            guiGraphics.renderTooltip(
                    font,
                    Component.literal(tooltip),
                    mouseX,
                    mouseY
            );
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
