package net.noah.redstoneplus;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraftforge.registries.ForgeRegistries;

public class RedstoneLinkEntityRenderer implements BlockEntityRenderer<RedstoneLinkEntity> {
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public RedstoneLinkEntityRenderer(BlockEntityRendererProvider.Context ctx)  {

    }

    @Override
    public void render(RedstoneLinkEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:"+ pBlockEntity.serializeNBT().getString("channelId")));
        ItemStack itemStack = new ItemStack(item);

        pPoseStack.pushPose();
        float scale = 0.7f;
        float offset = 0.16f;

        switch ((AttachFace) pBlockEntity.getBlockState().getValue(FACE)) {
            case FLOOR:
                pPoseStack.translate(0.5d, offset,0.5d);
                pPoseStack.scale(scale,scale,scale);
                pPoseStack.mulPose(Axis.XP.rotationDegrees(90));
                break;
            case WALL:
                switch ((Direction) pBlockEntity.getBlockState().getValue(FACING)) {
                    case EAST:
                        pPoseStack.translate(offset, 0.5d,0.5d);
                        pPoseStack.scale(scale,scale,scale);
                        pPoseStack.mulPose(Axis.YP.rotationDegrees(90));
                        break;
                    case WEST:
                        pPoseStack.translate(1-offset, 0.5d,0.5d);
                        pPoseStack.scale(scale,scale,scale);
                        pPoseStack.mulPose(Axis.YP.rotationDegrees(90));
                        break;
                    case SOUTH:
                        pPoseStack.translate(0.5d, 0.5d,offset);
                        pPoseStack.scale(scale,scale,scale);
                        break;
                    case NORTH:
                    default:
                        pPoseStack.translate(0.5d, 0.5d,1-offset);
                        pPoseStack.scale(scale,scale,scale);
                        break;
                }
                break;
            case CEILING:
            default:
                pPoseStack.translate(0.5d, 1-offset,0.5d);
                pPoseStack.scale(scale,scale,scale);
                pPoseStack.mulPose(Axis.XP.rotationDegrees(90));
        }


        itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, getLightLevel(pBlockEntity.getLevel(), pBlockEntity.getBlockPos()),
                OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, pBlockEntity.getLevel(), 1);
        pPoseStack.popPose();
    }

    private int getLightLevel(Level level, BlockPos blockPos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, blockPos);
        int sLight = level.getBrightness(LightLayer.SKY, blockPos);
        return LightTexture.pack(bLight, sLight);
    }
}
