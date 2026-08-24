package com.dogika.lh.network;

import com.dogika.lh.LinkedHeaven;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record SelectGroupPayload(UUID groupId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SelectGroupPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LinkedHeaven.MODID, "select_group"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SelectGroupPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.fromCodec(net.minecraft.core.UUIDUtil.CODEC), SelectGroupPayload::groupId,
        SelectGroupPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
