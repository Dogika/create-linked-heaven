package com.dogika.lh.compat.create;

import com.dogika.lh.LinkedHeaven;
import com.dogika.lh.group.GroupAssignment;
import com.dogika.lh.group.LinkGroup;
import com.dogika.lh.network.ShowGroupOutlinePayload;
import com.dogika.lh.registry.ModAttachments;
import com.simibubi.create.Create;
import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

public final class RedstoneLinkGroupBinder {

    private static final int OUTLINE_DURATION_TICKS = 40;

    private RedstoneLinkGroupBinder() {
    }

    public static boolean bind(ServerLevel level, BlockPos pos, BlockState state, LinkGroup group,
                               ServerPlayer player) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SmartBlockEntity smart)) {
            return false;
        }
        LinkBehaviour link = smart.getBehaviour(LinkBehaviour.TYPE);
        if (link == null) {
            return false;
        }

        Create.REDSTONE_LINK_NETWORK_HANDLER.removeFromNetwork(level, link);
        GroupAssignment assignment = group.isGlobal()
                ? GroupAssignment.GLOBAL
                : new GroupAssignment(group.id(), false);
        smart.setData(ModAttachments.LINK_GROUP.get(), assignment);
        smart.setChanged();
        smart.sendData();

        Create.REDSTONE_LINK_NETWORK_HANDLER.addToNetwork(level, link);

        Component feedback = group.isGlobal()
                ? Component.translatable("action."+ LinkedHeaven.MODID+".cleared")
                : Component.translatable("action."+ LinkedHeaven.MODID+".bound", group.name());
        player.displayClientMessage(feedback.copy().withStyle(ChatFormatting.GREEN), true);
        PacketDistributor.sendToPlayer(player, new ShowGroupOutlinePayload(pos, OUTLINE_DURATION_TICKS));
        return true;
    }
}