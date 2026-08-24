package com.dogika.lh.target;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public final class YawUtil {

    private YawUtil() {
    }

    public static Vector3d worldHeadDirection(Level level, BlockPos pos, float localHeadYaw) {
        SubLevelAccess sub = SableCompanion.INSTANCE.getContaining(level, pos);
        Vector3d localDir = JOMLConversion.toJOML(Vec3.directionFromRotation(0, localHeadYaw));
        return sub == null ? localDir : rotateYaw(localDir, sub);
    }

    private static Vector3d rotateYaw(Vector3d localDir, SubLevelAccess subLevel) {
        return subLevel.logicalPose().transformNormal(localDir, new Vector3d());
    }

    public static Vec3 playerWorldForward(Player player) {
        return Vec3.directionFromRotation(player.getXRot(), player.getYRot());
    }

    public static Vec3 fakeDistantTarget(Vec3 origin, Vec3 worldForward, double distance) {
        return origin.add(worldForward.scale(distance));
    }

}