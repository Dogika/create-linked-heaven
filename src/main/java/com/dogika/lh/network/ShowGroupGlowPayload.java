package com.dogika.lh.network;

import com.dogika.lh.LinkedHeaven;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ShowGroupGlowPayload(BlockPos pos, int durationTicks, int color) implements CustomPacketPayload {

    public static final Type<ShowGroupGlowPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LinkedHeaven.MODID, "show_group_glow"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShowGroupGlowPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, ShowGroupGlowPayload::pos,
        ByteBufCodecs.VAR_INT, ShowGroupGlowPayload::durationTicks,
        ByteBufCodecs.VAR_INT, ShowGroupGlowPayload::color,
        ShowGroupGlowPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
