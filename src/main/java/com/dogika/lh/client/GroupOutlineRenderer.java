package com.dogika.lh.client;

import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;

final class GroupOutlineRenderer {

    private static final int COLOR = 0x2ECC71;
    private static final float LINE_WIDTH = 1 / 16f;

    private GroupOutlineRenderer() {
    }

    static void show(BlockPos pos, int durationTicks) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        VoxelShape shape = level.getBlockState(pos).getShape(level, pos);
        if (shape.isEmpty()) {
            return;
        }
        Outliner.getInstance()
            .showAABB(pos, shape.bounds().move(pos), durationTicks)
            .colored(COLOR)
            .lineWidth(LINE_WIDTH);
    }
}
