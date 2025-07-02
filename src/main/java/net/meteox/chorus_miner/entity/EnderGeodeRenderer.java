package net.meteox.chorus_miner.entity;

import net.meteox.chorus_miner.ChorusMiner;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class EnderGeodeRenderer extends ThrownItemRenderer<EnderGeodeEntity> {
    public EnderGeodeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
