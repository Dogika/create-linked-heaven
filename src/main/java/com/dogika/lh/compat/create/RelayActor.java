
package com.dogika.lh.compat.create;

import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;

final class RelayActor implements IRedstoneLinkable {

    private final Couple<Frequency> networkKey;
    private volatile BlockPos location = BlockPos.ZERO;
    private volatile int reportedStrength = 0;

    RelayActor(Couple<Frequency> networkKey) {
        this.networkKey = networkKey;
    }

    void updatePosition(BlockPos newLocation) {
        this.location = newLocation;
    }

    boolean updateReportedStrength(int newStrength) {
        if (newStrength == reportedStrength) {
            return false;
        }
        reportedStrength = newStrength;
        return true;
    }

    @Override
    public int getTransmittedStrength() {
        return reportedStrength;
    }

    @Override
    public void setReceivedStrength(int power) {
    }

    @Override
    public boolean isListening() {
        return false;
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public Couple<Frequency> getNetworkKey() {
        return networkKey;
    }

    @Override
    public BlockPos getLocation() {
        return location;
    }
}