package com.dogika.lh.registry;

import com.dogika.lh.LinkedHeaven;
import com.dogika.lh.group.GroupAssignment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {

    public static final String LINK_GROUP_NBT_KEY = LinkedHeaven.MODID + ":link_group";

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, LinkedHeaven.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<GroupAssignment>> LINK_GROUP = ATTACHMENTS.register(
            "link_group",
            () -> AttachmentType.builder(() -> GroupAssignment.GLOBAL)
                    .serialize(GroupAssignment.CODEC)
                    .build());

    private ModAttachments() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}