package net.noah.redstonelink;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;


public class RedstoneLinkEntityRenderer implements BlockEntityRenderer<RedstoneLinkEntity> {
    public RedstoneLinkEntityRenderer(BlockEntityRendererProvider.Context ctx)  {

    }

    @Override
    public void render(RedstoneLinkEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
//        if (!pBlockEntity.hasLevel() || pBlockEntity.getBlockState().getBlock() == Blocks.AIR) {
//            return;
//        }
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
//        System.out.println("minecraft:"+ pBlockEntity.serializeNBT());
//        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:"+ pBlockEntity.serializeNBT().getString("channelId")));
//        assert item != null;
        ItemStack itemStack = pBlockEntity.getRenderStack();
        System.out.println(itemStack.getItem().toString());

        pPoseStack.pushPose();
        pPoseStack.translate(0.5f, 0f, 0.5f);
        pPoseStack.scale(1.0f, 1.0f, 1.0f);
        pPoseStack.mulPose(Axis.XP.rotationDegrees(270));

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
