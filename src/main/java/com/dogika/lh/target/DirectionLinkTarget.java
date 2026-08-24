package com.dogika.lh.target;

import com.dogika.lh.registry.ModComponents;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;
import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.NavigationTarget;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public class DirectionLinkTarget implements NavigationTarget {

    public static final DirectionLinkTarget INSTANCE = new DirectionLinkTarget();
    private static final double DISTANT = 1000;

    @Override
    @Nullable
    public Vec3 getTarget(NavTableBlockEntity navBE, ItemStack self) {
        LinkedHead linked = self.get(ModComponents.LINKED_HEAD.get());
        if (linked == null) {
            return null;
        }
        MinecraftServer server = navBE.getLevel().getServer();
        ServerLevel headLevel = server == null ? null : server.getLevel(linked.dimension());
        if (headLevel == null || !headLevel.isLoaded(linked.pos())) {
            return null;
        }
        Float localYaw = readHeadYaw(headLevel.getBlockState(linked.pos()));
        if (localYaw == null) {
            return null;
        }

        Vector3d worldHeadDirection = YawUtil.worldHeadDirection(headLevel, linked.pos(), localYaw);

        return YawUtil.fakeDistantTarget(navBE.getProjectedSelfPos(), JOMLConversion.toMojang(worldHeadDirection), DISTANT);
    }

    @Nullable
    public static Float readHeadYaw(BlockState state) {
        if (state.getBlock() instanceof SkullBlock && state.hasProperty(SkullBlock.ROTATION)) {
            return state.getValue(SkullBlock.ROTATION) * 22.5F + 180.0F;
        }
        if (state.getBlock() instanceof WallSkullBlock && state.hasProperty(WallSkullBlock.FACING)) {
            return state.getValue(WallSkullBlock.FACING).toYRot();
        }
        return null;
    }
}