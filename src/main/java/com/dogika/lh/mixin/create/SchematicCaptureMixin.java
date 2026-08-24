
package com.dogika.lh.mixin.create;

import com.dogika.lh.group.GroupAssignment;
import com.dogika.lh.registry.ModAttachments;
import com.mojang.serialization.DataResult;
import com.simibubi.create.content.schematics.SchematicAndQuillItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(SchematicAndQuillItem.class)
public abstract class SchematicCaptureMixin {

    private static final String ATTACHMENTS_KEY = "neoforge:attachments";

    @Inject(method = "clampGlueBoxes", at = @At("TAIL"))
    private static void frequencygroups$flattenGroups(Level level, AABB aabb, CompoundTag nbt, CallbackInfo ci) {
        if (!nbt.contains("blocks", Tag.TAG_LIST)) {
            return;
        }
        Map<UUID, UUID> realIdToMarker = new HashMap<>();
        ListTag blocks = nbt.getList("blocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < blocks.size(); i++) {
            CompoundTag blockEntry = blocks.getCompound(i);
            if (blockEntry.contains("nbt", Tag.TAG_COMPOUND)) {
                frequencygroups$flattenOne(blockEntry.getCompound("nbt"), realIdToMarker);
            }
        }
    }

    private static void frequencygroups$flattenOne(CompoundTag beTag, Map<UUID, UUID> realIdToMarker) {
        if (!beTag.contains(ATTACHMENTS_KEY, Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag attachments = beTag.getCompound(ATTACHMENTS_KEY);
        if (!attachments.contains(ModAttachments.LINK_GROUP_NBT_KEY)) {
            return;
        }

        DataResult<GroupAssignment> parsed =
                GroupAssignment.CODEC.parse(NbtOps.INSTANCE, attachments.get(ModAttachments.LINK_GROUP_NBT_KEY));
        GroupAssignment assignment = parsed.result().orElse(null);
        if (assignment == null || assignment.isGlobal() || assignment.isSchematicMarker()) {
            return;
        }

        UUID marker = realIdToMarker.computeIfAbsent(assignment.id(), realId -> UUID.randomUUID());
        GroupAssignment markerAssignment = new GroupAssignment(marker, true);
        GroupAssignment.CODEC.encodeStart(NbtOps.INSTANCE, markerAssignment).result()
                .ifPresent(encoded -> attachments.put(ModAttachments.LINK_GROUP_NBT_KEY, encoded));
    }
}