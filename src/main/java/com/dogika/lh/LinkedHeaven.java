package com.dogika.lh;

import com.dogika.lh.group.ServerRegistryAccess;
import com.dogika.lh.item.ModItems;
import com.dogika.lh.network.ModNetworking;
import com.dogika.lh.registry.ModAttachments;
import com.dogika.lh.registry.ModComponents;
import com.dogika.lh.target.ModNavigationTargets;
import com.mojang.logging.LogUtils;
import dev.simulated_team.simulated.index.SimDataComponents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

@Mod(LinkedHeaven.MODID)
public class LinkedHeaven {

    public static final String MODID = "lh";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final boolean SIMULATED_LOADED = ModList.get().isLoaded("simulated");

    public LinkedHeaven(IEventBus modEventBus, ModContainer modContainer) {
        ModAttachments.register(modEventBus);
        ModComponents.register(modEventBus);
        ModItems.register(modEventBus);
        ModNetworking.register(modEventBus);
        if (SIMULATED_LOADED) {
            ModNavigationTargets.register(modEventBus);
            modEventBus.addListener(LinkedHeaven::attachTargets);
        }

        NeoForge.EVENT_BUS.addListener((ServerStartingEvent event) ->
                ServerRegistryAccess.setServer(event.getServer()));
        NeoForge.EVENT_BUS.addListener((ServerStoppingEvent event) ->
                ServerRegistryAccess.clear());
    }

    private static void attachTargets(ModifyDefaultComponentsEvent event) {
        event.modify(ModItems.DIRECTION_LINK.get(),
                b -> b.set(SimDataComponents.TARGET, ModNavigationTargets.DIRECTION_LINK.get()));
        event.modify(ModItems.PILOT_LINK.get(),
                b -> b.set(SimDataComponents.TARGET, ModNavigationTargets.PILOT_LINK.get()));
    }
}
