package com.dogika.lh.mixin.create;

import com.dogika.lh.compat.create.CrossLevelLinkTracker;
import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RedstoneLinkNetworkHandler.class)
public abstract class RedstoneLinkNetworkHandlerMixin {

    @Inject(method = "addToNetwork", at = @At("TAIL"))
    private void frequencygroups$onAdd(LevelAccessor world, IRedstoneLinkable actor, CallbackInfo ci) {
        CrossLevelLinkTracker.onActorRegistered(world, actor);
    }

    @Inject(method = "removeFromNetwork", at = @At("HEAD"))
    private void frequencygroups$onRemove(LevelAccessor world, IRedstoneLinkable actor, CallbackInfo ci) {
        CrossLevelLinkTracker.onActorUnregistered(world, actor);
    }

    @Inject(method = "updateNetworkOf", at = @At("TAIL"))
    private void frequencygroups$onUpdate(LevelAccessor world, IRedstoneLinkable actor, CallbackInfo ci) {
        CrossLevelLinkTracker.onRealActorUpdated(world, actor);
    }
}