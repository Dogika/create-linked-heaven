
package com.dogika.lh.mixin.create;

import com.dogika.lh.group.GroupAssignment;
import com.dogika.lh.group.LinkGroup;
import com.dogika.lh.group.LinkGroupRegistry;
import com.dogika.lh.group.ServerRegistryAccess;
import com.dogika.lh.registry.ModAttachments;
import com.mojang.serialization.DataResult;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(SchematicannonBlockEntity.class)
public abstract class SchematicannonBlockEntityMixin {

    private static final String ATTACHMENTS_KEY = "neoforge:attachments";

    @Unique
    private final Map<UUID, LinkGroup> frequencygroups$markerToAbstractGroup = new HashMap<>();

    @Inject(method = "resetPrinter", at = @At("TAIL"))
    private void frequencygroups$onReset(CallbackInfo ci) {
        frequencygroups$markerToAbstractGroup.clear();
    }

    @Inject(method = "launchBlock", at = @At("HEAD"))
    private void frequencygroups$remap(BlockPos target, ItemStack stack, BlockState state, CompoundTag data,
                                       CallbackInfo ci) {
        if (data == null || !data.contains(ATTACHMENTS_KEY, Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag attachments = data.getCompound(ATTACHMENTS_KEY);
        if (!attachments.contains(ModAttachments.LINK_GROUP_NBT_KEY)) {
            return;
        }

        DataResult<GroupAssignment> parsed =
                GroupAssignment.CODEC.parse(NbtOps.INSTANCE, attachments.get(ModAttachments.LINK_GROUP_NBT_KEY));
        GroupAssignment assignment = parsed.result().orElse(null);
        if (assignment == null || assignment.isGlobal()) {
            return;
        }

        LinkGroupRegistry registry = ServerRegistryAccess.registryOrNull();
        if (registry == null) {
            return;
        }

        LinkGroup abstractGroup = frequencygroups$markerToAbstractGroup.computeIfAbsent(assignment.id(),
                id -> registry.createAbstractGroup());

        GroupAssignment replacement = new GroupAssignment(abstractGroup.id(), false);
        GroupAssignment.CODEC.encodeStart(NbtOps.INSTANCE, replacement).result()
                .ifPresent(encoded -> attachments.put(ModAttachments.LINK_GROUP_NBT_KEY, encoded));
    }
}