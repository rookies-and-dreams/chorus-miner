package net.meteox.chorus_miner.block.entity;

import net.meteox.chorus_miner.item.ModItems;
import net.meteox.chorus_miner.screen.ChorusMinerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChorusMinerBlockEntity extends BlockEntity implements MenuProvider {
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    private int cookMaxProgress = 200; // 10 seconds at 20 ticks per second
    private int chargeMaxProgress = 16; // how many chorus fruit it takes to fill the charge

    protected final ContainerData containerData;

    private int cookProgress = 0;
    private int chargeProgress = 0;


    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private LazyOptional<ItemStackHandler> lazyItemHandler = LazyOptional.of(() -> itemHandler);

    public ChorusMinerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHORUS_MINER_BE.get(), pos, state);
        this.containerData = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> ChorusMinerBlockEntity.this.cookProgress;
                    case 1 -> ChorusMinerBlockEntity.this.cookMaxProgress;
                    case 2 -> ChorusMinerBlockEntity.this.chargeProgress;
                    case 3 -> ChorusMinerBlockEntity.this.chargeMaxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch(pIndex){
                    case 0 -> ChorusMinerBlockEntity.this.cookProgress = pValue;
                    case 1 -> ChorusMinerBlockEntity.this.cookMaxProgress = pValue;
                    case 2 -> ChorusMinerBlockEntity.this.chargeProgress = pValue;
                    case 3 -> ChorusMinerBlockEntity.this.chargeMaxProgress = pValue;
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for(int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.chorus_miner.chorus_miner");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new ChorusMinerMenu(pContainerId, pPlayerInventory, this, this.containerData);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("inventory", itemHandler.serializeNBT());
        pTag.putInt("chorus_miner.cook_progress", cookProgress);
        pTag.putInt("chorus_miner.charge_progress", chargeProgress);

        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemHandler.deserializeNBT(pTag.getCompound("inventory"));
        cookProgress = pTag.getInt("chorus_miner.cook_progress");
        chargeProgress = pTag.getInt("chorus_miner.charge_progress");
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if(hasRecipe()) {
            increaseCraftingProgress();
            setChanged(pLevel, pPos, pState);

            if(hasProgressFinished()) {

                this.itemHandler.extractItem(INPUT_SLOT, 1, false);
                increaseChargeProgress();

                if (hasChargeFilled())
                {
                    craftItem();
                    resetChargeProgress();
                }

                resetCookProgress();
            }
        } else {
            resetCookProgress();
        }
    }

    private void resetCookProgress() {
        cookProgress = 0;
    }

    private void resetChargeProgress() {
        chargeProgress = 0;
    }

    private void craftItem() {
        ItemStack result = new ItemStack(ModItems.ENDER_GEODE.get(), 1);
        this.itemHandler.extractItem(INPUT_SLOT, 1, false);

        this.itemHandler.setStackInSlot(OUTPUT_SLOT, new ItemStack(result.getItem(),
                this.itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + result.getCount()));
    }

    private boolean hasRecipe() {
        boolean hasCraftingItem = this.itemHandler.getStackInSlot(INPUT_SLOT).getItem() == Items.CHORUS_FRUIT.asItem();
        ItemStack result = new ItemStack(ModItems.ENDER_GEODE.get());

        return hasCraftingItem && canInsertAmountIntoOutputSlot(result.getCount()) && canInsertItemIntoOutputSlot(result.getItem());
    }

    private boolean canInsertItemIntoOutputSlot(Item item) {
        return this.itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() || this.itemHandler.getStackInSlot(OUTPUT_SLOT).is(item);
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        return this.itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + count <= this.itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
    }

    private boolean hasProgressFinished() {
        return cookProgress >= cookMaxProgress;
    }

    private boolean hasChargeFilled() { return chargeProgress >= chargeMaxProgress; }

    private void increaseCraftingProgress() {
        cookProgress++;
    }

    private void increaseChargeProgress() { chargeProgress++; }
}
