package net.noah.redstoneplus;

import com.sun.jna.platform.win32.WinDef;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.units.qual.C;

import javax.sound.sampled.Clip;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import static net.noah.redstoneplus.RedstoneLinkBlock.IS_SENDER;
import static net.noah.redstoneplus.RedstoneLinkBlock.POWERED;

public class EnderWatcherBlockEntity extends BlockEntity {
    public EnderWatcherBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(RedstonePlus.ENDER_WATCHER_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (pLevel.isClientSide) {return;}

        int range = 80;
        List<Player> players = pLevel.getEntitiesOfClass(Player.class, new AABB(pPos.offset(-range, -range, -range), pPos.offset(range, range, range)));

        for (Player p : players) {
            HitResult result = raytrace(p, pLevel);
            System.out.println(result.getType());
            System.out.println(((BlockHitResult)result).getBlockPos());
            if (result != null && result.getType() == HitResult.Type.BLOCK && ((BlockHitResult) result).getBlockPos().equals(pPos)) {
                System.out.println("Ender watcher found");
                pLevel.setBlock(pPos, pState.setValue(POWERED, true), 3);
                return;
            }
        }
        pLevel.setBlock(pPos, pState.setValue(POWERED, false), 3);
    }

    private static HitResult raytrace (Entity pPlayer, Level pLevel) {
        Vec3 startPos = new Vec3(pPlayer.getX(), pPlayer.getEyeY(), pPlayer.getZ());
        Vec3 endPos = startPos.add(pPlayer.getLookAngle().scale(64));
        return pLevel.clip(new ClipContext(startPos, endPos, Block.OUTLINE, Fluid.NONE, pPlayer));
    }
}
