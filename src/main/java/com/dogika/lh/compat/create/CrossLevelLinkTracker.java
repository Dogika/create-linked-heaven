
package com.dogika.lh.compat.create;

import com.dogika.lh.group.GroupTokens;
import com.simibubi.create.Create;
import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

public final class CrossLevelLinkTracker {

    private static final CrossLevelLinkTracker INSTANCE = new CrossLevelLinkTracker();

    private final Map<Couple<Frequency>, Map<Level, Set<BlockPos>>> membersByKeyAndLevel = new HashMap<>();
    private final Map<Couple<Frequency>, Map<Level, RelayActor>> relaysByKeyAndLevel = new HashMap<>();

    private CrossLevelLinkTracker() {
    }

    public static void onActorRegistered(LevelAccessor world, IRedstoneLinkable actor) {
        if (!(actor instanceof LinkBehaviour link) || !(world instanceof Level level)) {
            return;
        }
        Couple<Frequency> key = link.getNetworkKey();
        if (!isGroupedKey(key)) {
            return;
        }
        INSTANCE.addMember(key, level, link.getLocation());
    }

    public static void onActorUnregistered(LevelAccessor world, IRedstoneLinkable actor) {
        if (!(actor instanceof LinkBehaviour link) || !(world instanceof Level level)) {
            return;
        }
        Couple<Frequency> key = link.getNetworkKey();
        if (!isGroupedKey(key)) {
            return;
        }
        INSTANCE.removeMember(key, level, link.getLocation());
    }

    public static void onRealActorUpdated(LevelAccessor world, IRedstoneLinkable actor) {
        if (!(actor instanceof LinkBehaviour link) || !(world instanceof Level level)) {
            return;
        }
        Couple<Frequency> key = link.getNetworkKey();
        if (!isGroupedKey(key)) {
            return;
        }
        INSTANCE.propagateIfCrossLevel(key, level);
    }

    public static void clearAll() {
        INSTANCE.membersByKeyAndLevel.clear();
        INSTANCE.relaysByKeyAndLevel.clear();
    }

    private void addMember(Couple<Frequency> key, Level level, BlockPos pos) {
        membersByKeyAndLevel.computeIfAbsent(key, k -> new IdentityHashMap<>())
                .computeIfAbsent(level, l -> new HashSet<>())
                .add(pos.immutable());
        reconcileRelays(key);
    }

    private void removeMember(Couple<Frequency> key, Level level, BlockPos pos) {
        Map<Level, Set<BlockPos>> byLevel = membersByKeyAndLevel.get(key);
        if (byLevel == null) {
            return;
        }
        Set<BlockPos> positions = byLevel.get(level);
        if (positions != null) {
            positions.remove(pos);
            if (positions.isEmpty()) {
                byLevel.remove(level);
            }
        }
        if (byLevel.isEmpty()) {
            membersByKeyAndLevel.remove(key);
        }
        reconcileRelays(key);
    }

    private void reconcileRelays(Couple<Frequency> key) {
        Map<Level, Set<BlockPos>> byLevel = membersByKeyAndLevel.get(key);
        Map<Level, RelayActor> relays = relaysByKeyAndLevel.computeIfAbsent(key, k -> new IdentityHashMap<>());

        if (byLevel == null || byLevel.size() <= 1) {
            for (Map.Entry<Level, RelayActor> entry : relays.entrySet()) {
                unregisterRelay(entry.getKey(), entry.getValue());
            }
            relays.clear();
            relaysByKeyAndLevel.remove(key);
            return;
        }

        relays.keySet().removeIf(level -> {
            if (byLevel.containsKey(level)) {
                return false;
            }
            unregisterRelay(level, relays.get(level));
            return true;
        });
        for (Level level : byLevel.keySet()) {
            relays.computeIfAbsent(level, l -> registerRelay(l, key));
        }

        int globalMax = computeGlobalMax(byLevel);
        for (Map.Entry<Level, RelayActor> entry : relays.entrySet()) {
            Level level = entry.getKey();
            RelayActor relay = entry.getValue();
            relay.updatePosition(centroidOf(byLevel.get(level)));
            if (relay.updateReportedStrength(globalMax)) {
                Create.REDSTONE_LINK_NETWORK_HANDLER.updateNetworkOf(level, relay);
            }
        }
    }

    private void propagateIfCrossLevel(Couple<Frequency> key, Level sourceLevel) {
        Map<Level, Set<BlockPos>> byLevel = membersByKeyAndLevel.get(key);
        if (byLevel == null || byLevel.size() <= 1) {
            return;
        }
        Map<Level, RelayActor> relays = relaysByKeyAndLevel.get(key);
        if (relays == null) {
            return;
        }
        int globalMax = computeGlobalMax(byLevel);
        for (Map.Entry<Level, RelayActor> entry : relays.entrySet()) {
            if (entry.getValue().updateReportedStrength(globalMax)) {
                Create.REDSTONE_LINK_NETWORK_HANDLER.updateNetworkOf(entry.getKey(), entry.getValue());
            }
        }
    }

    private RelayActor registerRelay(Level level, Couple<Frequency> key) {
        RelayActor relay = new RelayActor(key);
        Create.REDSTONE_LINK_NETWORK_HANDLER.addToNetwork(level, relay);
        return relay;
    }

    private void unregisterRelay(Level level, RelayActor relay) {
        if (relay != null) {
            Create.REDSTONE_LINK_NETWORK_HANDLER.removeFromNetwork(level, relay);
        }
    }

    private int computeGlobalMax(Map<Level, Set<BlockPos>> byLevel) {
        int max = 0;
        outer:
        for (Map.Entry<Level, Set<BlockPos>> entry : byLevel.entrySet()) {
            for (BlockPos pos : entry.getValue()) {
                LinkBehaviour link = realLinkAt(entry.getKey(), pos);
                if (link != null && !link.isListening()) {
                    max = Math.max(max, link.getTransmittedStrength());
                    if (max >= 15) {
                        break outer;
                    }
                }
            }
        }
        return max;
    }

    private static LinkBehaviour realLinkAt(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof com.simibubi.create.foundation.blockEntity.SmartBlockEntity smart)) {
            return null;
        }
        return smart.getBehaviour(LinkBehaviour.TYPE);
    }

    private static BlockPos centroidOf(Set<BlockPos> positions) {
        if (positions.isEmpty()) {
            return BlockPos.ZERO;
        }
        long sx = 0;
        long sy = 0;
        long sz = 0;
        for (BlockPos p : positions) {
            sx += p.getX();
            sy += p.getY();
            sz += p.getZ();
        }
        int n = positions.size();
        return new BlockPos(
                (int) Math.round((double) sx / n),
                (int) Math.round((double) sy / n),
                (int) Math.round((double) sz / n));
    }

    private static boolean isGroupedKey(Couple<Frequency> key) {
        ItemStack first = key.get(true).getStack();
        ItemStack second = key.get(false).getStack();
        return GroupTokens.isToken(first) && GroupTokens.isToken(second);
    }
}