package net.noah.redstoneplus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RedstoneLinkBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final BooleanProperty IS_SENDER = BooleanProperty.create("is_sender");
    public static final VoxelShape FLOOR = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 2.0D, 14.0D);
    protected static final VoxelShape NORTH_AABB = Block.box(2.0D, 2.0D, 14.0D, 14.0D, 14.0D, 16.0D);
    protected static final VoxelShape SOUTH_AABB = Block.box(2.0D, 2.0D, 0.0D, 14.0D, 14.0D, 2.0D);
    protected static final VoxelShape WEST_AABB = Block.box(14.0D, 2.0D, 2.0D, 16.0D, 14.0D, 14.0D);
    protected static final VoxelShape EAST_AABB = Block.box(0.0D, 2.0D, 2.0D, 2.0D, 14.0D, 14.0D);
    protected static final VoxelShape DOWN = Block.box(2.0D, 14.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public RedstoneLinkBlock(Properties pProperties) {
        super(pProperties);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FACE, AttachFace.WALL)
                .setValue(POWERED, false));
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        switch ((AttachFace) pState.getValue(FACE)) {
            case FLOOR:
                return FLOOR;
            case WALL:
                switch ((Direction) pState.getValue(FACING)) {
                    case EAST:
                        return EAST_AABB;
                    case WEST:
                        return WEST_AABB;
                    case SOUTH:
                        return SOUTH_AABB;
                    case NORTH:
                    default:
                        return NORTH_AABB;
                }
            case CEILING:
            default:
                return DOWN;
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, POWERED, IS_SENDER, FACE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        for(Direction direction : pContext.getNearestLookingDirections()) {
            BlockState blockstate;
            if (direction.getAxis() == Direction.Axis.Y) {
                blockstate = this.defaultBlockState().setValue(FACE, direction == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR).setValue(FACING, pContext.getHorizontalDirection());
            } else {
                blockstate = this.defaultBlockState().setValue(FACE, AttachFace.WALL).setValue(FACING, direction.getOpposite());
            }

            if (blockstate.canSurvive(pContext.getLevel(), pContext.getClickedPos())) {
                return blockstate.setValue(IS_SENDER, true)
                        .setValue(POWERED, pContext.getLevel().hasNeighborSignal(pContext.getClickedPos()));
            }
        }

        return null;
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide) {
            BlockState blockstate1 = pState.cycle(IS_SENDER);
            return InteractionResult.SUCCESS;
        }

        if (pPlayer.getMainHandItem().isEmpty()) {                             //Sender <-> Receiver
            if (pState.getValue(IS_SENDER)) {                                  //Sender->Receiver, remove channel entry
                ChannelManager.removeByBlockPos(pPos, (ServerLevel) pLevel);
            } else {                                                           //Receiver->Sender, add channel entry
                ChannelManager.updateByBlockPos(pLevel.getBlockEntity(pPos).serializeNBT().getString("channelId"), pPos, pLevel.hasNeighborSignal(pPos), (ServerLevel) pLevel);
                pState.setValue(POWERED, pLevel.hasNeighborSignal(pPos));
            }
            pState = pState.cycle(IS_SENDER);
            pLevel.setBlock(pPos, pState, 3);
            float f = pState.getValue(IS_SENDER) ? 0.6F : 0.5F;
            pLevel.playSound((Player)null, pPos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, f);
            return InteractionResult.CONSUME;
        }

        ((RedstoneLinkEntity) pLevel.getBlockEntity(pPos)).setChannelId(pPlayer.getMainHandItem().getItem().toString());
        pLevel.getBlockEntity(pPos).setChanged();
        if (pState.getValue(IS_SENDER)) {
            ChannelManager.updateByBlockPos(pPlayer.getMainHandItem().getItem().toString(), pPos, pState.getValue(POWERED).booleanValue(), (ServerLevel) pLevel);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    @Deprecated
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pLevel instanceof ServerLevel serverLevel) {
            ChannelManager.removeByBlockPos(pPos, serverLevel);
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        if (pLevel.isClientSide) {return;}

        if (!pState.canSurvive(pLevel,pPos)) {
            dropResources(pState, pLevel, pPos);
            pLevel.removeBlock(pPos, false);
            return;
        }

        if (!pState.getValue(IS_SENDER)) {
            return;
        }

        if (pLevel.hasNeighborSignal(pPos) && !pState.getValue(POWERED)) {
            pLevel.setBlock(pPos, pState.cycle(POWERED), 2);
            ChannelManager.updateByBlockPos(pLevel.getBlockEntity(pPos).serializeNBT().getString("channelId"), pPos,true, (ServerLevel) pLevel);
            return;
        }

        if (!pLevel.hasNeighborSignal(pPos) && pState.getValue(POWERED)) {
            pLevel.setBlock(pPos, pState.cycle(POWERED), 2);
            ChannelManager.updateByBlockPos(pLevel.getBlockEntity(pPos).serializeNBT().getString("channelId"), pPos, false, (ServerLevel) pLevel);
        }
    }

    @Override
    public boolean canConnectRedstone(BlockState pstate, BlockGetter plevel, BlockPos pPos, @Nullable Direction pDirection) {
        return true;
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState pState, SignalGetter pLevel, BlockPos pPos, Direction pDirection) {
        return false;
    }

    @Override
    public boolean isSignalSource(BlockState pState) {
        return !pState.getValue(IS_SENDER);
    }

    @Override
    public int getSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
        if (pState.getValue(IS_SENDER)) {
            return 0;
        }
        if (pState.getValue(POWERED)) {
            return 15;
        }
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new RedstoneLinkEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }

        return createTickerHelper(pBlockEntityType, RedstonePlus.REDSTONE_LINK_BLOCK_ENTITY.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1));
    }
}
