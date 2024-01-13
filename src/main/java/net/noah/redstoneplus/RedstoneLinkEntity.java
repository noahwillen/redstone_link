package net.noah.redstoneplus;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;


import java.util.Objects;

import static net.noah.redstoneplus.RedstoneLinkBlock.*;

public class RedstoneLinkEntity extends BlockEntity {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private String channelId="";

    public RedstoneLinkEntity(BlockPos pPos, BlockState pBlockState) {
        super(RedstonePlus.REDSTONE_LINK_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (pLevel.isClientSide) {return;}
        if (!pState.getValue(IS_SENDER)) {
            boolean powered = ChannelManager.getPower(channelId, (ServerLevel) pLevel);
            pLevel.setBlock(pPos, pState.setValue(POWERED, powered), 2);
            pLevel.updateNeighborsAt(pPos, Objects.requireNonNull(pLevel.getBlockEntity(pPos)).getBlockState().getBlock());
        }
        this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
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

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        saveAdditional(nbt);
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

}
