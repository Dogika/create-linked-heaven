package com.dogika.lh.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record GroupEntry(UUID id, String name, String creatorName, boolean locked, boolean mine) {

    private static final int MAX_NAME_CHARS = 48;

    public static final StreamCodec<RegistryFriendlyByteBuf, GroupEntry> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.fromCodec(net.minecraft.core.UUIDUtil.CODEC), GroupEntry::id,
        ByteBufCodecs.stringUtf8(MAX_NAME_CHARS), GroupEntry::name,
        ByteBufCodecs.stringUtf8(MAX_NAME_CHARS), GroupEntry::creatorName,
        ByteBufCodecs.BOOL, GroupEntry::locked,
        ByteBufCodecs.BOOL, GroupEntry::mine,
        GroupEntry::new);
}
