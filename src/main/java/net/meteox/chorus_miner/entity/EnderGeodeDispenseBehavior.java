package net.meteox.chorus_miner.entity;

import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;

public class EnderGeodeDispenseBehavior extends DefaultDispenseItemBehavior{
    @Override
    protected ItemStack execute(BlockSource pSource, ItemStack pStack) {
        Level level = pSource.getLevel();
        Direction direction = pSource.getBlockState().getValue(DispenserBlock.FACING);
        Vec3 position = pSource.getPos().getCenter().relative(direction, 0.5);

        if (!level.isClientSide) {
            // Spawn your custom entity here
            EnderGeodeEntity entity = new EnderGeodeEntity(level, position.x, position.y, position.z);
            entity.shoot(direction.getStepX(), direction.getStepY(), direction.getStepZ(), 0.5F, 1.0F);
            level.addFreshEntity(entity);

            // Optionally: reduce stack by 1
            pStack.shrink(1);
        }

        return pStack;
    }
}

