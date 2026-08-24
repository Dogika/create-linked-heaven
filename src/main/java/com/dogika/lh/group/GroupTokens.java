package com.dogika.lh.group;

import com.dogika.lh.LinkedHeaven;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DyedItemColor;

public final class GroupTokens {

    private GroupTokens() {
    }

    public static ItemStack tokenForColor(int color) {
        ItemStack stack = new ItemStack(Items.STRUCTURE_VOID);
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color, false));
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putBoolean(LinkedHeaven.MODID+"_group_token", true);
        });
        return stack;
    }

    public static boolean isToken(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != Items.STRUCTURE_VOID) {
            return false;
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData != null && customData.contains(LinkedHeaven.MODID+"_group_token");
    }
}