package me.questoria.qtrap.model;

import java.util.UUID;

public record TrapLogEntry(
        String trapId,
        UUID actor,
        String actorName,
        String action,
        String detail,
        long createdAt
) {
}
