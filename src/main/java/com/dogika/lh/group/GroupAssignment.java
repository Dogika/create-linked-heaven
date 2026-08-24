package com.dogika.lh.group;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public record GroupAssignment(UUID id, boolean isSchematicMarker) {

    public static final GroupAssignment GLOBAL = new GroupAssignment(LinkGroup.GLOBAL_ID, false);

    public static final Codec<GroupAssignment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("Id").forGetter(GroupAssignment::id),
            Codec.BOOL.optionalFieldOf("Marker", false).forGetter(GroupAssignment::isSchematicMarker)
    ).apply(instance, GroupAssignment::new));

    public boolean isGlobal() {
        return !isSchematicMarker && id.equals(LinkGroup.GLOBAL_ID);
    }
}