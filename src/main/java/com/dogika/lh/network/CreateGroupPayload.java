package com.dogika.lh.network;

import com.dogika.lh.LinkedHeaven;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CreateGroupPayload(String name, boolean lockImmediately) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CreateGroupPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LinkedHeaven.MODID, "create_group"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CreateGroupPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.stringUtf8(48), CreateGroupPayload::name,
        ByteBufCodecs.BOOL, CreateGroupPayload::lockImmediately,
        CreateGroupPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
