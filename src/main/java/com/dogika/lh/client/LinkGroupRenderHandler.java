package com.dogika.lh.client;

import com.dogika.lh.LinkedHeaven;
import com.dogika.lh.LinkedHeavenClientBridge;
import com.dogika.lh.group.GroupAssignment;
import com.dogika.lh.network.ModNetworking;
import com.dogika.lh.registry.ModAttachments;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.content.redstone.link.RedstoneLinkBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.joml.Matrix4f;


@EventBusSubscriber(modid = LinkedHeaven.MODID, value = Dist.CLIENT)
public class LinkGroupRenderHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ModNetworking.resync(serverPlayer, false);
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;

        Minecraft mc = Minecraft.getInstance();
        if (!(mc.player instanceof LocalPlayer localPlayer) || !GogglesItem.isWearingGoggles(localPlayer) || mc.level == null) return;

        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult blockHitResult)) return;

        BlockPos pos = blockHitResult.getBlockPos();
        Camera camera = event.getCamera();

        if (camera.getPosition().distanceToSqr(Vec3.atCenterOf(pos)) > 10 * 10) return;
        if (!(mc.level.getBlockState(pos).getBlock() instanceof RedstoneLinkBlock)) return;
        if (!(mc.level.getBlockEntity(pos) instanceof SmartBlockEntity smart)) return;

        GroupAssignment assignment = smart.getData(ModAttachments.LINK_GROUP.get());
        String name = LinkedHeavenClientBridge.getGroupName(assignment.id());
        Component text = Component.literal(name);

        PoseStack poseStack = event.getPoseStack();

        Vec3 cameraPos = camera.getPosition();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        poseStack.pushPose();
        poseStack.translate(pos.getX() - cameraPos.x + 0.5, pos.getY() - cameraPos.y + 0.5, pos.getZ() - cameraPos.z + 0.5);
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = poseStack.last().pose();
        Font font = mc.font;
        float textWidthOffset = (float)(-font.width(text) / 2);
        float textHeightOffset = (float)(-font.lineHeight / 2);
        font.drawInBatch(text, textWidthOffset, textHeightOffset, -1, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);

        poseStack.popPose();
        bufferSource.endBatch();
    }

    public static void renderText(Component text, Vec3 pos, PoseStack poseStack) {

    }
}