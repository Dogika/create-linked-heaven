package com.dogika.lh.registry;

import com.dogika.lh.LinkedHeaven;
import com.dogika.lh.target.LinkedHead;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;

public final class ModComponents {

    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, LinkedHeaven.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> SELECTED_GROUP =
            COMPONENTS.registerComponentType(
                    "selected_group",
                    builder -> builder.persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC));

    private static final StreamCodec<RegistryFriendlyByteBuf, LinkedHead> LINKED_HEAD_STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), LinkedHead::dimension,
            net.minecraft.core.BlockPos.STREAM_CODEC, LinkedHead::pos,
            LinkedHead::new);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LinkedHead>> LINKED_HEAD =
            COMPONENTS.registerComponentType("linked_head", builder -> builder
                    .persistent(LinkedHead.CODEC)
                    .networkSynchronized(LINKED_HEAD_STREAM_CODEC));



    private ModComponents() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
