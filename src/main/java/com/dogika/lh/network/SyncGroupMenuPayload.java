package com.dogika.lh.network;

import com.dogika.lh.LinkedHeaven;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.UUID;

public record SyncGroupMenuPayload(List<GroupEntry> groups, UUID selectedGroupId, boolean openScreen) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncGroupMenuPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LinkedHeaven.MODID, "sync_group_menu"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncGroupMenuPayload> STREAM_CODEC = StreamCodec.composite(
        GroupEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncGroupMenuPayload::groups,
        ByteBufCodecs.fromCodec(net.minecraft.core.UUIDUtil.CODEC), SyncGroupMenuPayload::selectedGroupId,
        ByteBufCodecs.BOOL, SyncGroupMenuPayload::openScreen,
        SyncGroupMenuPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
