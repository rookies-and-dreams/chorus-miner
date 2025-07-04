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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChorusMinerBlockEntity extends BlockEntity implements MenuProvider {

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    private int cookMaxProgress = 100; // 5 seconds at 20 ticks per second
    private int chargeMaxProgress = 8; // how many chorus fruit it takes to fill the charge

    protected final ContainerData containerData;

    private int cookProgress = 0;
    private int chargeProgress = 0;


    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();

            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private LazyOptional<IItemHandler>[] sidedHandlers = ChorusMinerSidedHandlers.create(this);

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
        if (cap == ForgeCapabilities.ITEM_HANDLER && side != null) {
            // Handlers are invalidated and created all at once
            // If one's dead, they should all be dead
            sidedHandlers = ChorusMinerSidedHandlers.create(this);

            return sidedHandlers[side.ordinal()].cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        lazyItemHandler = LazyOptional.of(() -> itemHandler);

        // Reinitialize sided handlers after chunk reload
        sidedHandlers = ChorusMinerSidedHandlers.create(this);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();

        for (LazyOptional<IItemHandler> handler : sidedHandlers) {
            handler.invalidate();
        }

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

    public ItemStackHandler getItemHandler() { return itemHandler; }

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

// Sided handlers for Chorus Miner!
class ChorusMinerSidedHandlers {
    public static LazyOptional<IItemHandler>[] create(ChorusMinerBlockEntity blockEntity) {
        @SuppressWarnings("unchecked")
        LazyOptional<IItemHandler>[] handlers = new LazyOptional[6];

        for (Direction direction : Direction.values()) {
            handlers[direction.ordinal()] = LazyOptional.of(() -> new IItemHandler() {
                @Override
                public int getSlots() {
                    return blockEntity.getItemHandler().getSlots();
                }

                @Override
                public @NotNull ItemStack getStackInSlot(int slot) {
                    return blockEntity.getItemHandler().getStackInSlot(slot);
                }

                @Override
                public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                    // DO NOT INSERT IN OUPUT, believe me, been there done that
                    // but every day above ground is a great day, remember that
                    // I KNEW MY RENT WAS GONNA BE--
                    if (slot != 0) return stack;

                    if (direction == Direction.DOWN) return stack; // No insert from bottom
                    if (stack.getItem() != Items.CHORUS_FRUIT) return stack; // Only allow chorus fruit

                    return blockEntity.getItemHandler().insertItem(slot, stack, simulate);
                }

                @Override
                public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                    // DO NOT EXTRACT FROM INPUT SLOT
                    // THIS FOR ERRBODY GOING THRU TOUGH--
                    if (slot != 1) return ItemStack.EMPTY;

                    ItemStack stack = blockEntity.getItemHandler().getStackInSlot(slot);
                    if (direction == Direction.DOWN && stack.getItem() == ModItems.ENDER_GEODE.get()) {
                        return blockEntity.getItemHandler().extractItem(slot, amount, simulate);
                    }
                    return ItemStack.EMPTY;
                }

                @Override
                public int getSlotLimit(int slot) {
                    return blockEntity.getItemHandler().getSlotLimit(slot);
                }

                @Override
                public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                    return stack.getItem() == Items.CHORUS_FRUIT;
                }
            });
        }

        return handlers;
    }
}
