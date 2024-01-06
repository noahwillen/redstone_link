package net.noah.redstonelink;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.registries.ForgeRegistries;


import java.util.Objects;

import static net.noah.redstonelink.RedstoneLinkBlock.IS_SENDER;

public class RedstoneLinkEntity extends BlockEntity {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    private String channelId="";

    public RedstoneLinkEntity(BlockPos pPos, BlockState pBlockState) {
        super(RedstoneLink.REDSTONE_LINK_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    public void tick(Level pLevel1, BlockPos pPos, BlockState pState1) {
        if (pLevel1.isClientSide) {return;}
        if (!pState1.getValue(IS_SENDER)) {
            int newSignal = ChannelManager.getHighestSignalStrength(channelId, (ServerLevel) pLevel1);
            pLevel1.setBlock(pPos, pState1.setValue(POWER, newSignal), 2);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.putString("channelId", channelId);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        channelId = pTag.getString("channelId");
    }
    public void setChannelId(String channelId) {
        this.channelId=channelId;
    }

    public ItemStack getRenderStack() {
        return new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:" +this.channelId))));
    }
}
