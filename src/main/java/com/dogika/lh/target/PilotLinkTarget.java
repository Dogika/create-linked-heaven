package com.dogika.lh.target;

import com.dogika.lel.owner.PersonalOwner;
import com.dogika.lel.registry.ModComponents;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;
import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.NavigationTarget;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class PilotLinkTarget implements NavigationTarget {

    public static final PilotLinkTarget INSTANCE = new PilotLinkTarget();
    private static final double DISTANT = 1000;

    @Override
    @Nullable
    public Vec3 getTarget(NavTableBlockEntity navBE, ItemStack self) {
        PersonalOwner owner = self.get(ModComponents.OWNER.get());
        if (owner == null) {
            return null;
        }
        MinecraftServer server = navBE.getLevel().getServer();
        ServerPlayer player = server == null ? null : server.getPlayerList().getPlayer(owner.id());
        if (player == null) {
            return null;
        }

        Vec3 playerDirection = YawUtil.playerWorldForward(player);
        return YawUtil.fakeDistantTarget(navBE.getProjectedSelfPos(), playerDirection, DISTANT);
    }
}