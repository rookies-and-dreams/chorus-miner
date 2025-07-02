package net.meteox.chorus_miner.entity;

import net.meteox.chorus_miner.ChorusMiner;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntity {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ChorusMiner.MOD_ID);

    public static final RegistryObject<EntityType<EnderGeodeEntity>> ENDER_GEODE =
            ENTITY_TYPES.register("ender_geode", () -> EntityType.Builder
                    .<EnderGeodeEntity>of(EnderGeodeEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("ender_geode"));

    public static void register(IEventBus modEventBus)
    {
        ENTITY_TYPES.register(modEventBus);
    }
}
