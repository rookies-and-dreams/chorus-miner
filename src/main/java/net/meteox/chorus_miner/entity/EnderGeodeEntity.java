package net.meteox.chorus_miner.entity;

import net.meteox.chorus_miner.ChorusMiner;
import net.meteox.chorus_miner.item.ModItems;
import net.meteox.chorus_miner.sound.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class EnderGeodeEntity  extends ThrowableItemProjectile {

    private final ResourceLocation ENDER_GEODE_LOOT_TABLE = new ResourceLocation(ChorusMiner.MOD_ID, "ender_geode");

    public EnderGeodeEntity(EntityType<? extends EnderGeodeEntity> type, Level level) {
        super(type, level);
    }

    public EnderGeodeEntity(Level level, LivingEntity thrower) {
        super(ModEntity.ENDER_GEODE.get(), thrower, level);
    }

    public EnderGeodeEntity(Level level, double x, double y, double z) {
        super(ModEntity.ENDER_GEODE.get(), x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.ENDER_GEODE.get(); // your custom item
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        result.getEntity().hurt(damageSources().thrown(this, this.getOwner()), 2.0F);
        if (!this.level().isClientSide) {
            breakGeode();
            this.discard(); // remove after hit
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            breakGeode();
            this.discard();
        }
    }

    private void breakGeode() {
        ServerLevel serverLevel = (ServerLevel) this.level();

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                ModSounds.ENDER_GEODE_BREAK.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        dropLoot();
    }

    private void dropLoot() {
        ServerLevel serverLevel = (ServerLevel) this.level();

        // Server side: generate loot and give to player
        LootTable lootTable = this.level().getServer()
                .getLootData()
                .getLootTable(ENDER_GEODE_LOOT_TABLE);

        LootParams.Builder builder = new LootParams.Builder((ServerLevel)  this.level())
                .withParameter(LootContextParams.ORIGIN, this.position())
                .withParameter(LootContextParams.THIS_ENTITY, this)
                .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, this.getOwner());

        LootParams context = builder.create(LootContextParamSets.GIFT);
        List<ItemStack> loot = lootTable.getRandomItems(context);

        for (ItemStack stack : loot) {
            ItemEntity itemEntity = new ItemEntity(serverLevel, this.getX(), this.getY(), this.getZ(), stack);
            serverLevel.addFreshEntity(itemEntity);
        }
    }
}

