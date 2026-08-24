package com.dogika.lh.compat.create;

import com.dogika.lh.group.*;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import net.createmod.catnip.data.Couple;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;


public final class ChannelTransform {

    private ChannelTransform() {
    }

    public static ItemStack transformOne(UUID groupId, ItemStack nativeStack) {
        if (groupId == null || groupId.equals(LinkGroup.GLOBAL_ID)) {
            return null;
        }
        LinkGroupRegistry registry = ServerRegistryAccess.registryOrNull();
        if (registry == null) {
            return null;
        }
        Frequency real = Frequency.of(nativeStack);
        int color = registry.allocateOrGetChannelColor(groupId, real);
        return GroupTokens.tokenForColor(color);
    }

    public static Couple<Frequency> transform(GroupAssignment assignment, Couple<Frequency> nativeCouple) {
        if (assignment == null || assignment.isGlobal() || assignment.isSchematicMarker()) {
            return null;
        }
        ItemStack firstTransformed = transformOne(assignment.id(), nativeCouple.get(true).getStack());
        ItemStack secondTransformed = transformOne(assignment.id(), nativeCouple.get(false).getStack());
        if (firstTransformed == null || secondTransformed == null) {
            return null;
        }
        return Couple.create(Frequency.of(firstTransformed), Frequency.of(secondTransformed));
    }
}