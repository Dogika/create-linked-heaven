package com.dogika.lh.network;

import com.dogika.lh.LinkedHeaven;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ShowGroupOutlinePayload(BlockPos pos, int durationTicks) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ShowGroupOutlinePayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LinkedHeaven.MODID, "show_group_outline"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShowGroupOutlinePayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, ShowGroupOutlinePayload::pos,
        ByteBufCodecs.VAR_INT, ShowGroupOutlinePayload::durationTicks,
        ShowGroupOutlinePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
