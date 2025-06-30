package net.meteox.chorus_miner.item;

import net.meteox.chorus_miner.ChorusMiner;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ChorusMiner.MOD_ID);

    public static final RegistryObject<Item> ENDER_GEODE = ITEMS.register("ender_geode",
            () -> new EnderGeode(new Item.Properties().stacksTo(16)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
