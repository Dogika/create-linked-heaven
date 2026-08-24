package com.dogika.lh.item;

import com.dogika.lh.LinkedHeaven;
import com.simibubi.create.content.redstone.link.RedstoneLinkBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = LinkedHeaven.MODID)
public class AutoBindEventHandler {

    private static final List<Runnable> DELAYED_TASKS = new ArrayList<>();

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && event.getEntity() instanceof ServerPlayer player) {

            BlockState placedState = event.getPlacedBlock();

            if (placedState.getBlock() instanceof RedstoneLinkBlock) {
                ItemStack offhandItem = player.getItemInHand(InteractionHand.OFF_HAND);

                if (offhandItem.getItem() instanceof FrequencyInterfaceItem frequencyItem) {

                    BlockPos pos = event.getPos();

                    DELAYED_TASKS.add(() -> {
                        BlockState currentState = serverLevel.getBlockState(pos);
                        if (currentState.getBlock() instanceof RedstoneLinkBlock) {
                            frequencyItem.tryBind(player, serverLevel, pos, currentState, offhandItem, false);
                        }
                    });
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!DELAYED_TASKS.isEmpty()) {
            for (Runnable task : DELAYED_TASKS) {
                task.run();
            }
            DELAYED_TASKS.clear();
        }
    }
}
