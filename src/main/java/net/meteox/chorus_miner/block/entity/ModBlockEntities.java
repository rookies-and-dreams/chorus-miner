package net.meteox.chorus_miner.block.entity;

import net.meteox.chorus_miner.ChorusMiner;
import net.meteox.chorus_miner.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ChorusMiner.MOD_ID);

    public static final RegistryObject<BlockEntityType<ChorusMinerBlockEntity>> CHORUS_MINER_BE =
            BLOCK_ENTITIES.register("chorus_miner_be", () ->
                    BlockEntityType.Builder.of(ChorusMinerBlockEntity::new,
                            ModBlocks.CHORUS_MINER.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
