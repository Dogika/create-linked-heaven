package com.dogika.lh.network;

import com.dogika.lh.LinkedHeavenClientBridge;
import com.dogika.lh.client.BlockGlowHighlight;
import com.dogika.lh.group.LinkGroup;
import com.dogika.lh.group.LinkGroupRegistry;
import com.dogika.lh.item.FrequencyInterfaceItem;
import com.dogika.lh.registry.ModComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;
import java.util.UUID;

public final class ModNetworking {

    private ModNetworking() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ModNetworking::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(SyncGroupMenuPayload.TYPE, SyncGroupMenuPayload.STREAM_CODEC, ModNetworking::handleSyncMenu);
        registrar.playToClient(ShowGroupOutlinePayload.TYPE, ShowGroupOutlinePayload.STREAM_CODEC, ModNetworking::handleShowOutline);
        registrar.playToClient(ShowGroupGlowPayload.TYPE, ShowGroupGlowPayload.STREAM_CODEC, ModNetworking::handleShowGlow);
        registrar.playToServer(CreateGroupPayload.TYPE, CreateGroupPayload.STREAM_CODEC, ModNetworking::handleCreateGroup);
        registrar.playToServer(SelectGroupPayload.TYPE, SelectGroupPayload.STREAM_CODEC, ModNetworking::handleSelectGroup);
        registrar.playToServer(SetGroupLockedPayload.TYPE, SetGroupLockedPayload.STREAM_CODEC, ModNetworking::handleSetLocked);
        registrar.playToServer(RequestGroupListPayload.TYPE, RequestGroupListPayload.STREAM_CODEC, ModNetworking::handleRequestGroupList);
    }

    private static void handleSyncMenu(SyncGroupMenuPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> LinkedHeavenClientBridge.syncMenu(payload.groups(), payload.selectedGroupId(), payload.openScreen()));
    }

    private static void handleShowOutline(ShowGroupOutlinePayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> LinkedHeavenClientBridge.showOutline(payload.pos(), payload.durationTicks()));
    }

    private static void handleShowGlow(ShowGroupGlowPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> BlockGlowHighlight.show(payload.pos(), payload.durationTicks(), payload.color()));
    }

    private static void handleCreateGroup(CreateGroupPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            LinkGroupRegistry registry = LinkGroupRegistry.get(player.getServer());
            LinkGroup created = registry.createNamedGroup(payload.name(), player.getUUID(),
                    player.getGameProfile().getName(), payload.lockImmediately());
            if (created != null) {
                setSelectedGroupIfHoldingItem(player, created.id());
            }
            resync(player, false);
        });
    }

    private static void handleSelectGroup(SelectGroupPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            LinkGroupRegistry registry = LinkGroupRegistry.get(player.getServer());
            LinkGroup group = registry.byId(payload.groupId());
            if (group != null && group.usableBy(player.getUUID())) {
                setSelectedGroupIfHoldingItem(player, group.id());
            }
            resync(player, false);
        });
    }

    private static void handleSetLocked(SetGroupLockedPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            LinkGroupRegistry registry = LinkGroupRegistry.get(player.getServer());
            registry.trySetLocked(payload.groupId(), player.getUUID(), payload.locked());
            resync(player, false);
        });
    }

    private static void handleRequestGroupList(RequestGroupListPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                resync(player, false);
            }
        });
    }

    public static void openMenuFor(ServerPlayer player) {
        resync(player, true);
    }

    public static void resync(ServerPlayer player, boolean openScreen) {
        LinkGroupRegistry registry = LinkGroupRegistry.get(player.getServer());
        UUID selected = currentSelection(player);
        List<GroupEntry> entries = registry.search("").stream()
                .map(g -> new GroupEntry(g.id(), g.name(), g.creatorName() == null ? "" : g.creatorName(),
                        g.locked(), g.creatorId() != null && g.creatorId().equals(player.getUUID())))
                .toList();
        PacketDistributor.sendToPlayer(player, new SyncGroupMenuPayload(entries, selected, openScreen));
    }

    private static UUID currentSelection(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof FrequencyInterfaceItem)) {
            return LinkGroup.GLOBAL_ID;
        }
        UUID selected = held.get(ModComponents.SELECTED_GROUP.get());
        return selected == null ? LinkGroup.GLOBAL_ID : selected;
    }

    private static void setSelectedGroupIfHoldingItem(ServerPlayer player, UUID groupId) {
        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof FrequencyInterfaceItem) {
            held.set(ModComponents.SELECTED_GROUP.get(), groupId);
        }
    }
}