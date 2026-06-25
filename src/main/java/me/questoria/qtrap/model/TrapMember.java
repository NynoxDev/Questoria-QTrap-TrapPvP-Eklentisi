package me.questoria.qtrap.model;

import java.util.UUID;

public final class TrapMember {
    private final UUID uuid;
    private TrapRole role;

    public TrapMember(UUID uuid, TrapRole role) {
        this.uuid = uuid;
        this.role = role;
    }

    public UUID uuid() {
        return uuid;
    }

    public TrapRole role() {
        return role;
    }

    public void role(TrapRole role) {
        this.role = role;
    }
}
