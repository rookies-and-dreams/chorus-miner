package net.meteox.chorus_miner.item;

import net.meteox.chorus_miner.ChorusMiner;
import net.meteox.chorus_miner.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ChorusMiner.MOD_ID);

    public static final RegistryObject<CreativeModeTab> CHORUS_MINER_TAB = CREATIVE_MODE_TABS.register("chorus_miner_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.CHORUS_MINER.get()))
                    .title(Component.translatable("creativetab.chorus_miner_tab"))
                    .displayItems((itemsDisplayParameter, output) ->
                    {
                        output.accept(ModItems.ENDER_GEODE.get());

                        output.accept(ModBlocks.CHORUS_MINER.get());
                    })
                    .build());

    public static void register(IEventBus eventBus)
    {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
