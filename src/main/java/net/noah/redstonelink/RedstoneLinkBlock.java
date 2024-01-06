package net.noah.redstonelink;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.noah.redstonelink.RedstoneLink;
import net.noah.redstonelink.RedstoneLinkEntity;
import net.noah.redstonelink.ChannelManager;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

public class RedstoneLinkBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final BooleanProperty IS_SENDER = BooleanProperty.create("is_sender");
    private boolean shouldSignal = true;
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
                .setValue(POWER, 0)
                .setValue(FACE, AttachFace.WALL));
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

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, POWER, IS_SENDER, FACE);
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
                return blockstate.setValue(POWER, 0)
                        .setValue(IS_SENDER, true);
            }
        }

        return null;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide) {
            BlockState blockstate1 = pState.cycle(IS_SENDER);
            return InteractionResult.SUCCESS;
        }
        if (pPlayer.getMainHandItem().is(Items.BLAZE_ROD)) {  //Remove
            System.out.println(ChannelManager.getData((ServerLevel) pLevel));
            return InteractionResult.CONSUME;
        }


        if (pPlayer.getMainHandItem().isEmpty()) {
            pState = pState.cycle(IS_SENDER);
            pLevel.setBlock(pPos, pState, 3);
            this.updateNeighbours(pState, pLevel, pPos);
            float f = pState.getValue(IS_SENDER) ? 0.6F : 0.5F;
            pLevel.playSound((Player)null, pPos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, f);
            return InteractionResult.CONSUME;
        }

        ((RedstoneLinkEntity) pLevel.getBlockEntity(pPos)).setChannelId(pPlayer.getMainHandItem().getItem().toString());
        pLevel.getBlockEntity(pPos).setChanged();
        if (pState.getValue(IS_SENDER)) {
            ChannelManager.updateByBlockPos(pPlayer.getMainHandItem().getItem().toString(), pPos, pState.getValue(POWER).intValue(), (ServerLevel) pLevel);
        }
        return InteractionResult.CONSUME;
    }


    private void updateNeighbours(BlockState pState, Level pLevel, BlockPos pPos) {
        pLevel.updateNeighborsAt(pPos, this);
        pLevel.updateNeighborsAt(pPos.relative(getConnectedDirection(pState).getOpposite()), this);
    }

    protected static Direction getConnectedDirection(BlockState pState) {
        switch ((AttachFace)pState.getValue(FACE)) {
            case CEILING:
                return Direction.DOWN;
            case FLOOR:
                return Direction.UP;
            default:
                return pState.getValue(FACING);
        }
    }

    @Override
    @Deprecated
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pLevel instanceof ServerLevel serverLevel) {
            ChannelManager.removeByBlockPos(pPos, serverLevel);
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
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

        return createTickerHelper(pBlockEntityType, RedstoneLink.REDSTONE_LINK_BLOCK_ENTITY.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1));
    }

    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        if (!pLevel.isClientSide) {
            if (pState.canSurvive(pLevel, pPos)) {
                if (pState.getValue(IS_SENDER)) {
                    this.updatePowerStrength(pLevel, pPos, pState);
                    if (!Objects.requireNonNull(pLevel.getBlockEntity(pPos)).serializeNBT().getString("channelId").isEmpty()) {
                        ChannelManager.updateByBlockPos(pLevel.getBlockEntity(pPos).serializeNBT().getString("channelId"),
                                pPos, pState.getValue(POWER), (ServerLevel) pLevel);
                    }
                }
            } else {
                dropResources(pState, pLevel, pPos);
                pLevel.removeBlock(pPos, false);
            }

        }
    }

    private void updatePowerStrength(Level pLevel, BlockPos pPos, BlockState pState) {
        int i = this.calculateTargetStrength(pLevel, pPos);
        if (pState.getValue(POWER) != i) {
            if (pLevel.getBlockState(pPos) == pState) {
                pLevel.setBlock(pPos, pState.setValue(POWER, Integer.valueOf(i)), 2);
            }

            Set<BlockPos> set = Sets.newHashSet();
            set.add(pPos);

            for(Direction direction : Direction.values()) {
                set.add(pPos.relative(direction));
            }

            for(BlockPos blockpos : set) {
                pLevel.updateNeighborsAt(blockpos, this);
            }
        }

    }

    private int calculateTargetStrength(Level pLevel, BlockPos pPos) {
        this.shouldSignal = false;
        int i = pLevel.getBestNeighborSignal(pPos);
        this.shouldSignal = true;
        int j = 0;
        if (i < 15) {
            for(Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos blockpos = pPos.relative(direction);
                BlockState blockstate = pLevel.getBlockState(blockpos);
                j = Math.max(j, this.getWireSignal(blockstate));
                BlockPos blockpos1 = pPos.above();
                if (blockstate.isRedstoneConductor(pLevel, blockpos) && !pLevel.getBlockState(blockpos1).isRedstoneConductor(pLevel, blockpos1)) {
                    j = Math.max(j, this.getWireSignal(pLevel.getBlockState(blockpos.above())));
                } else if (!blockstate.isRedstoneConductor(pLevel, blockpos)) {
                    j = Math.max(j, this.getWireSignal(pLevel.getBlockState(blockpos.below())));
                }
            }
        }

        return Math.max(i, j - 1);
    }

    private int getWireSignal(BlockState pState) {
        return pState.is(this) ? pState.getValue(POWER) : 0;
    }
}
