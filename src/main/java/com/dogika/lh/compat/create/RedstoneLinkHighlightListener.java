package com.dogika.lh.compat.create;

import com.dogika.lh.LinkedHeaven;
import com.dogika.lh.item.FrequencyInterfaceItem;
import com.dogika.lh.network.ShowGroupGlowPayload;
import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.content.redstone.link.RedstoneLinkBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import com.dogika.lh.client.BlockGlowHighlight;

@EventBusSubscriber(modid = LinkedHeaven.MODID)
public final class RedstoneLinkHighlightListener {

    private static final int OUTLINE_DURATION_TICKS = 20 * 4;

    @SubscribeEvent
    private static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof ServerPlayer player) || !GogglesItem.isWearingGoggles(player) || player.isShiftKeyDown()) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof WrenchItem || stack.getItem() instanceof FrequencyInterfaceItem) {
            return;
        }

        ServerLevel level = (ServerLevel) event.getLevel();
        if (!(level.getBlockState(event.getPos()).getBlock() instanceof RedstoneLinkBlock)) {
            return;
        }
        BlockEntity be = level.getBlockEntity(event.getPos());
        if (!(be instanceof SmartBlockEntity smart)) {
            return;
        }
        LinkBehaviour link = smart.getBehaviour(LinkBehaviour.TYPE);
        if (link == null) {
            return;
        }

        event.setCanceled(true);

        int uniqueColor = BlockGlowHighlight.getColorForNetwork(link.getNetworkKey());

        for (IRedstoneLinkable actor : Create.REDSTONE_LINK_NETWORK_HANDLER.getNetworkOf(level, link)) {
            if (actor instanceof LinkBehaviour matched) {
                PacketDistributor.sendToPlayer(player,
                        new ShowGroupGlowPayload(matched.getLocation(), OUTLINE_DURATION_TICKS, uniqueColor));
            }
        }
    }
}