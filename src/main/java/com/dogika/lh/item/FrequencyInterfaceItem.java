package com.dogika.lh.item;

import com.dogika.lh.LinkedHeaven;
import com.dogika.lh.compat.create.RedstoneLinkGroupBinder;
import com.dogika.lh.group.LinkGroup;
import com.dogika.lh.group.LinkGroupRegistry;
import com.dogika.lh.network.ModNetworking;
import com.dogika.lh.registry.ModComponents;
import com.simibubi.create.content.redstone.link.RedstoneLinkBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class FrequencyInterfaceItem extends Item {

    private static final int ACTION_COOLDOWN_TICKS = 4;

    public FrequencyInterfaceItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();

        if (player == null) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                ModNetworking.openMenuFor(serverPlayer);
            }
            return InteractionResult.SUCCESS;
        }

        BlockState hitState = level.getBlockState(context.getClickedPos());
        if (hitState.getBlock() instanceof RedstoneLinkBlock) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                tryBind(serverPlayer, (ServerLevel) level, context.getClickedPos(), hitState, stack, true);
            }
            return InteractionResult.SUCCESS;
        }

        return use(level, player, context.getHand()).getResult();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                ModNetworking.openMenuFor(serverPlayer);
            }
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    public void tryBind(ServerPlayer player, ServerLevel level, net.minecraft.core.BlockPos pos, BlockState state,
                        ItemStack stack, boolean applyCooldown) {
        if (applyCooldown && player.getCooldowns().isOnCooldown(this)) {
            return;
        }

        UUID selectedId = stack.getOrDefault(ModComponents.SELECTED_GROUP.get(), LinkGroup.GLOBAL_ID);
        LinkGroupRegistry registry = LinkGroupRegistry.get(level.getServer());
        LinkGroup group = registry.byId(selectedId);

        if (group == null) {
            group = registry.byId(LinkGroup.GLOBAL_ID);
        }

        if (!group.usableBy(player.getUUID())) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("action."+ LinkedHeaven.MODID+".locked", group.name())
                            .withStyle(net.minecraft.ChatFormatting.RED),
                    true);
        } else {
            RedstoneLinkGroupBinder.bind(level, pos, state, group, player);
        }

        if (applyCooldown) {
            player.getCooldowns().addCooldown(this, ACTION_COOLDOWN_TICKS);
        }
    }
}