package com.dogika.lh.item;

import com.dogika.lh.client.LinkedHeavenClient;
import com.dogika.lh.registry.ModComponents;
import com.dogika.lh.target.DirectionLinkTarget;
import com.dogika.lh.target.LinkedHead;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class DirectionLinkItem extends Item {

    public DirectionLinkItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockState state = level.getBlockState(context.getClickedPos());
        if (DirectionLinkTarget.readHeadYaw(state) == null) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            LinkedHeavenClient.showOutline(context.getClickedPos(), 40);
        } else {
            ItemStack stack = context.getItemInHand();
            stack.set(ModComponents.LINKED_HEAD.get(), new LinkedHead(level.dimension(), context.getClickedPos().immutable()));
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        return InteractionResult.SUCCESS;
    }
}