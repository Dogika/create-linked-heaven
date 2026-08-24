package com.dogika.lh.target;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record LinkedHead(ResourceKey<Level> dimension, BlockPos pos) {
    public static final Codec<LinkedHead> CODEC = RecordCodecBuilder.create(i -> i.group(
            Level.RESOURCE_KEY_CODEC.fieldOf("Dimension").forGetter(LinkedHead::dimension),
            BlockPos.CODEC.fieldOf("Pos").forGetter(LinkedHead::pos)
    ).apply(i, LinkedHead::new));
}