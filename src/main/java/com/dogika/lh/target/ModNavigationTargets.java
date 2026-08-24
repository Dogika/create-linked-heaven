package com.dogika.lh.target;

import com.dogika.lh.LinkedHeaven;
import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.NavigationTarget;
import dev.simulated_team.simulated.index.SimRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModNavigationTargets {

    private static final DeferredRegister<NavigationTarget> TARGETS =
            DeferredRegister.create(SimRegistries.Keys.NAVIGATION_TARGET, LinkedHeaven.MODID);

    public static final DeferredHolder<NavigationTarget, DirectionLinkTarget> DIRECTION_LINK = LinkedHeaven.SIMULATED_LOADED
            ? TARGETS.register("direction_link", () -> DirectionLinkTarget.INSTANCE)
            : null;


    public static final DeferredHolder<NavigationTarget, PilotLinkTarget> PILOT_LINK = LinkedHeaven.SIMULATED_LOADED
            ? TARGETS.register("pilot_link", () -> PilotLinkTarget.INSTANCE)
            : null;

    private ModNavigationTargets() {
    }

    public static void register(IEventBus modBus) {
        TARGETS.register(modBus);
    }
}
