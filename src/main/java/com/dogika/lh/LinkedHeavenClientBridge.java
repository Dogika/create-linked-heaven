package com.dogika.lh;

import com.dogika.lh.client.LinkedHeavenClient;
import com.dogika.lh.network.GroupEntry;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class LinkedHeavenClientBridge {

    private static final Map<UUID, String> GROUP_NAME_CACHE = new HashMap<>();

    private LinkedHeavenClientBridge() {
    }

    public static void syncMenu(List<GroupEntry> groups, UUID selectedGroupId, boolean openScreen) {
        GROUP_NAME_CACHE.clear();
        for (GroupEntry entry : groups) {
            GROUP_NAME_CACHE.put(entry.id(), entry.name());
        }

        LinkedHeavenClient.syncMenu(groups, selectedGroupId, openScreen);
    }

    public static void showOutline(BlockPos pos, int durationTicks) {
        LinkedHeavenClient.showOutline(pos, durationTicks);
    }

    public static String getGroupName(UUID id) {
        if (id == null) {
            return "None";
        }
        return GROUP_NAME_CACHE.getOrDefault(id, "Unknown Group");
    }
}
