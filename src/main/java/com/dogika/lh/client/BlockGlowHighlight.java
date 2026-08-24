package com.dogika.lh.client;

import com.dogika.lh.LinkedHeaven;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import net.createmod.catnip.data.Couple;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LinkedHeaven.MODID, value = Dist.CLIENT)
public class BlockGlowHighlight {

    private static final float LINE_WIDTH = 3.0f;

    private static final RenderType GLOW_LINES = RenderType.create(
            LinkedHeaven.MODID+"_glow_lines",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderType.RENDERTYPE_LINES_SHADER)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(LINE_WIDTH)))
                    .setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(RenderType.TRANSLUCENT_TARGET)
                    .setWriteMaskState(RenderType.COLOR_WRITE)
                    .setCullState(RenderType.NO_CULL)
                    .setDepthTestState(RenderType.NO_DEPTH_TEST)
                    .createCompositeState(false)
    );

    private static final Map<BlockPos, int[]> ACTIVE = new ConcurrentHashMap<>();

    public static int getColorForNetwork(Couple<RedstoneLinkNetworkHandler.Frequency> networkKey) {
        int firstHash = networkKey.getFirst().hashCode();
        int secondHash = networkKey.getSecond().hashCode();

        int combinedHash = firstHash * 31 + secondHash;

        combinedHash ^= (combinedHash >>> 16);
        combinedHash *= 0x85ebca6b;
        combinedHash ^= (combinedHash >>> 13);

        float hue = (combinedHash & Integer.MAX_VALUE) / (float) Integer.MAX_VALUE;

        float saturation = 0.85f;
        float brightness = 1.0f;

        int rgb = Mth.hsvToRgb(hue, saturation, brightness);
        return rgb | 0xFF000000;
    }

    public static void show(BlockPos pos, int durationTicks, int uniqueColor) {
        ACTIVE.put(pos.immutable(), new int[]{uniqueColor, durationTicks});
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (ACTIVE.isEmpty()) return;
        ACTIVE.entrySet().removeIf(e -> --e.getValue()[1] <= 0);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES || ACTIVE.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 cam = event.getCamera().getPosition();

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(GLOW_LINES);

        for (Map.Entry<BlockPos, int[]> entry : ACTIVE.entrySet()) {
            BlockPos pos = entry.getKey();
            VoxelShape shape = level.getBlockState(pos).getShape(level, pos);
            if (shape.isEmpty()) continue;

            int color = entry.getValue()[0];
            float r = (color >> 16 & 0xFF) / 255f;
            float g = (color >> 8 & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            float a = (color >> 24 & 0xFF) / 255f;

            poseStack.pushPose();
            poseStack.translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);
            renderShape(poseStack, consumer, shape, 0, 0, 0, r, g, b, a);
            poseStack.popPose();
        }

        bufferSource.endBatch(GLOW_LINES);
    }

    static void renderShape(PoseStack poseStack, VertexConsumer consumer, VoxelShape shape, double x, double y, double z, float red, float green, float blue, float alpha) {
        PoseStack.Pose posestack$pose = poseStack.last();
        shape.forAllEdges((p_323073_, p_323074_, p_323075_, p_323076_, p_323077_, p_323078_) -> {
            float f = (float)(p_323076_ - p_323073_);
            float f1 = (float)(p_323077_ - p_323074_);
            float f2 = (float)(p_323078_ - p_323075_);
            float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
            f /= f3;
            f1 /= f3;
            f2 /= f3;

            consumer.addVertex(posestack$pose, (float)(p_323073_ + x), (float)(p_323074_ + y), (float)(p_323075_ + z))
                    .setColor(red, green, blue, alpha)
                    .setNormal(posestack$pose, f, f1, f2);

            consumer.addVertex(posestack$pose, (float)(p_323076_ + x), (float)(p_323077_ + y), (float)(p_323078_ + z))
                    .setColor(red, green, blue, alpha)
                    .setNormal(posestack$pose, f, f1, f2);
        });
    }
}