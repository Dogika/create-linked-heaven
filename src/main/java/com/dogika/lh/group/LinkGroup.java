package com.dogika.lh.group;

import java.util.UUID;

public final class LinkGroup {

    public static final UUID GLOBAL_ID = new UUID(0L, 0L);

    private final UUID id;
    private String name;
    private UUID creatorId;
    private String creatorName;
    private boolean locked;
    private final boolean abstractGroup;

    LinkGroup(UUID id, String name, UUID creatorId, String creatorName, boolean locked, boolean abstractGroup) {
        this.id = id;
        this.name = name;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.locked = locked;
        this.abstractGroup = abstractGroup;
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public UUID creatorId() {
        return creatorId;
    }

    public String creatorName() {
        return creatorName;
    }

    public boolean locked() {
        return locked;
    }

    void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isAbstract() {
        return abstractGroup;
    }

    public boolean isGlobal() {
        return id.equals(GLOBAL_ID);
    }

    public boolean usableBy(UUID playerId) {
        if (!locked) {
            return true;
        }
        return creatorId != null && creatorId.equals(playerId);
    }
}