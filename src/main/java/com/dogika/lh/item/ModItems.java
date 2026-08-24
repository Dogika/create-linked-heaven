package com.dogika.lh.item;

import com.dogika.lel.LivingExperienceLib;
import com.dogika.lel.client.ClientOnlyCode;
import com.dogika.lel.registry.ModComponents;
import com.dogika.lh.LinkedHeaven;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LinkedHeaven.MODID);

    public static final DeferredItem<FrequencyInterfaceItem> FREQUENCY_INTERFACE = ITEMS.registerItem(
            "frequency_interface", FrequencyInterfaceItem::new, new Item.Properties()
                    .stacksTo(1)
    );
        
    public static final DeferredItem<DirectionLinkItem> DIRECTION_LINK = LinkedHeaven.SIMULATED_LOADED
        ? ITEMS.registerItem(
            "direction_link", DirectionLinkItem::new, new Item.Properties()
                    .stacksTo(64)
        )
        : null;

    public static final DeferredItem<Item> PILOT_LINK = LinkedHeaven.SIMULATED_LOADED
        ? com.dogika.lel.item.ModItems.registerSimpleBoundItem(ITEMS, "pilot_link", DIRECTION_LINK)
        : null;



    private ModItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(ModItems::addItemsToLelTab);
    }

    public static void addItemsToLelTab(BuildCreativeModeTabContentsEvent event) {
        ResourceLocation lelTab = ResourceLocation.fromNamespaceAndPath(LivingExperienceLib.MODID, LivingExperienceLib.MODID+"_tab");

        if (event.getTabKey().location().equals(lelTab)) {

            ResourceLocation lhSectionId = ResourceLocation.fromNamespaceAndPath(LinkedHeaven.MODID, LinkedHeaven.MODID+"_section");

            for (var item : ITEMS.getEntries()) {
                ItemStack creativeTabItem = new ItemStack(item.get());

                if (FMLEnvironment.dist == Dist.CLIENT) {
                    ClientOnlyCode.applyClientPlayerOwner(item, creativeTabItem);
                }

                creativeTabItem.set(ModComponents.TAB_SECTION_ID.get(), lhSectionId);

                event.accept(creativeTabItem);
            }
        }
    }
}