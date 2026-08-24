package com.dogika.lh.group;

import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public final class LinkGroupRegistry extends SavedData {

    private static final String ID = "lh_link_registry";

    private final Map<UUID, LinkGroup> groups = new LinkedHashMap<>();
    private final Map<String, UUID> idByLowerName = new HashMap<>();
    private final Map<UUID, Map<Frequency, Integer>> channelColorsByGroup = new HashMap<>();
    private int nextChannelColor = 1;

    public LinkGroupRegistry() {
        LinkGroup global = new LinkGroup(LinkGroup.GLOBAL_ID, "Global", null, null, false, false);
        groups.put(global.id(), global);
        idByLowerName.put("global", global.id());
    }

    public static SavedData.Factory<LinkGroupRegistry> factory() {
        return new SavedData.Factory<>(LinkGroupRegistry::new, LinkGroupRegistry::load);
    }

    public static LinkGroupRegistry get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(factory(), ID);
    }

    public LinkGroup byId(UUID id) {
        return groups.get(id);
    }

    public boolean nameTaken(String rawName) {
        return idByLowerName.containsKey(rawName.strip().toLowerCase(Locale.ROOT));
    }

    public int namedGroupCount() {
        int count = 0;
        for (LinkGroup g : groups.values()) {
            if (!g.isGlobal() && !g.isAbstract()) {
                count++;
            }
        }
        return count;
    }

    public List<LinkGroup> search(String query) {
        String needle = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        List<LinkGroup> out = new ArrayList<>();
        for (LinkGroup g : groups.values()) {
            if (g.isAbstract()) {
                continue;
            }
            if (g.isGlobal() || needle.isEmpty() || g.name().toLowerCase(Locale.ROOT).contains(needle)) {
                out.add(g);
            }
        }
        out.sort((a, b) -> {
            if (a.isGlobal() != b.isGlobal()) {
                return a.isGlobal() ? -1 : 1;
            }
            return a.name().compareToIgnoreCase(b.name());
        });
        return out;
    }

    public LinkGroup createNamedGroup(String rawName, UUID creatorId, String creatorName, boolean lockedAtCreation) {
        String trimmed = rawName.strip();
        if (trimmed.isEmpty() || trimmed.length() > 48 || nameTaken(trimmed)) {
            return null;
        }
        LinkGroup group = new LinkGroup(UUID.randomUUID(), trimmed, creatorId, creatorName, lockedAtCreation, false);
        groups.put(group.id(), group);
        idByLowerName.put(trimmed.toLowerCase(Locale.ROOT), group.id());
        setDirty();
        return group;
    }

    public LinkGroup createAbstractGroup() {
        LinkGroup group = new LinkGroup(UUID.randomUUID(), null, null, null, false, true);
        groups.put(group.id(), group);
        setDirty();
        return group;
    }

    public boolean trySetLocked(UUID groupId, UUID requesterId, boolean locked) {
        LinkGroup group = groups.get(groupId);
        if (group == null || group.isGlobal() || group.isAbstract()) {
            return false;
        }
        if (group.creatorId() == null || !group.creatorId().equals(requesterId)) {
            return false;
        }
        group.setLocked(locked);
        setDirty();
        return true;
    }

    public int allocateOrGetChannelColor(UUID groupId, Frequency realFrequency) {
        Integer existing = channelColorsByGroup
                .computeIfAbsent(groupId, g -> new HashMap<>())
                .get(realFrequency);
        if (existing != null) {
            return existing;
        }
        int color = nextChannelColor++;
        channelColorsByGroup.get(groupId).put(realFrequency, color);
        setDirty();
        return color;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag groupList = new ListTag();
        for (LinkGroup group : groups.values()) {
            if (group.isGlobal()) {
                continue;
            }
            CompoundTag t = new CompoundTag();
            t.putUUID("Id", group.id());
            if (group.name() != null) {
                t.putString("Name", group.name());
            }
            if (group.creatorId() != null) {
                t.putUUID("CreatorId", group.creatorId());
            }
            if (group.creatorName() != null) {
                t.putString("CreatorName", group.creatorName());
            }
            t.putBoolean("Locked", group.locked());
            t.putBoolean("Abstract", group.isAbstract());
            groupList.add(t);
        }
        tag.put("Groups", groupList);

        ListTag channelList = new ListTag();
        for (Map.Entry<UUID, Map<Frequency, Integer>> byGroup : channelColorsByGroup.entrySet()) {
            for (Map.Entry<Frequency, Integer> entry : byGroup.getValue().entrySet()) {
                CompoundTag t = new CompoundTag();
                t.putUUID("GroupId", byGroup.getKey());
                t.put("Frequency", entry.getKey().getStack().saveOptional(registries));
                t.putInt("Color", entry.getValue());
                channelList.add(t);
            }
        }
        tag.put("Channels", channelList);
        tag.putInt("NextChannelColor", nextChannelColor);
        return tag;
    }

    private static LinkGroupRegistry load(CompoundTag tag, HolderLookup.Provider registries) {
        LinkGroupRegistry registry = new LinkGroupRegistry();

        ListTag groupList = tag.getList("Groups", Tag.TAG_COMPOUND);
        for (int i = 0; i < groupList.size(); i++) {
            CompoundTag t = groupList.getCompound(i);
            UUID id = t.getUUID("Id");
            String name = t.contains("Name") ? t.getString("Name") : null;
            UUID creatorId = t.hasUUID("CreatorId") ? t.getUUID("CreatorId") : null;
            String creatorName = t.contains("CreatorName") ? t.getString("CreatorName") : null;
            boolean locked = t.getBoolean("Locked");
            boolean isAbstract = t.getBoolean("Abstract");

            LinkGroup group = new LinkGroup(id, name, creatorId, creatorName, locked, isAbstract);

            registry.groups.put(id, group);
            if (name != null) {
                registry.idByLowerName.put(name.toLowerCase(Locale.ROOT), id);
            }
        }

        ListTag channelList = tag.getList("Channels", Tag.TAG_COMPOUND);
        for (int i = 0; i < channelList.size(); i++) {
            CompoundTag t = channelList.getCompound(i);
            UUID groupId = t.getUUID("GroupId");
            ItemStack stack = ItemStack.parseOptional(registries, t.getCompound("Frequency"));
            Frequency frequency = Frequency.of(stack);
            int color = t.getInt("Color");
            registry.channelColorsByGroup.computeIfAbsent(groupId, g -> new HashMap<>()).put(frequency, color);
        }
        registry.nextChannelColor = Math.max(1, tag.getInt("NextChannelColor"));
        return registry;
    }
}