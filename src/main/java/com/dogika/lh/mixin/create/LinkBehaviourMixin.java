package com.dogika.lh.mixin.create;

import com.dogika.lh.compat.create.ChannelTransform;
import com.dogika.lh.group.GroupAssignment;
import com.dogika.lh.registry.ModAttachments;
import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import net.createmod.catnip.data.Couple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LinkBehaviour.class)
public abstract class LinkBehaviourMixin {

    @Inject(method = "getNetworkKey", at = @At("RETURN"), cancellable = true)
    private void frequencygroups$transformKey(CallbackInfoReturnable<Couple<Frequency>> cir) {
        LinkBehaviour self = (LinkBehaviour) (Object) this;
        if (self.blockEntity == null) {
            return;
        }
        GroupAssignment assignment = self.blockEntity.getData(ModAttachments.LINK_GROUP.get());
        Couple<Frequency> transformed = ChannelTransform.transform(assignment, cir.getReturnValue());
        if (transformed != null) {
            cir.setReturnValue(transformed);
        }
    }
}