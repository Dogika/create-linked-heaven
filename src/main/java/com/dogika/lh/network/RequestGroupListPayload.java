package com.dogika.lh.network;

import com.dogika.lh.LinkedHeaven;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RequestGroupListPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RequestGroupListPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LinkedHeaven.MODID, "request_group_list"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestGroupListPayload> STREAM_CODEC =
        StreamCodec.unit(new RequestGroupListPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
