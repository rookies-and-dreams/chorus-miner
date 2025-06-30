package net.meteox.chorus_miner.item;

import net.meteox.chorus_miner.ChorusMiner;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;

public class EnderGeode extends Item {
    public EnderGeode(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand){
        ItemStack itemStack = player.getItemInHand(hand);

        if (!world.isClientSide) {
            // Server side: generate loot and give to player
            LootTable lootTable = world.getServer()
                    .getLootData()
                    .getLootTable(new ResourceLocation(ChorusMiner.MOD_ID, "ender_geode"));

            LootParams.Builder builder = new LootParams.Builder((ServerLevel) world)
                    .withParameter(LootContextParams.ORIGIN, player.position())
                    .withParameter(LootContextParams.THIS_ENTITY, player);

            LootParams context = builder.create(LootContextParamSets.GIFT);
            List<ItemStack> loot = lootTable.getRandomItems(context);

            for (ItemStack lootItem : loot) {
                if (!player.getInventory().add(lootItem)) {
                    player.drop(lootItem, false); // Drop if inventory is full
                }
            }

            // Consume the item
            if (!player.isCreative()) {
                itemStack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide);
    }
}
