package com.dogika.lh.client;

import com.dogika.lh.network.GroupEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.UUID;

public final class LinkedHeavenClient {

    private LinkedHeavenClient() {
    }

    public static void syncMenu(List<GroupEntry> groups, UUID selectedGroupId, boolean openScreen) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof FrequencyInterfaceScreen existing) {
            existing.updateData(groups, selectedGroupId);
        } else if (openScreen) {
            mc.setScreen(new FrequencyInterfaceScreen(groups, selectedGroupId));
        }
    }

    public static void showOutline(BlockPos pos, int durationTicks) {
        GroupOutlineRenderer.show(pos, durationTicks);
    }
}
