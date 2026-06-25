package me.questoria.qtrap.model;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.Objects;

public final class TrapChunk {
    private final String world;
    private final int x;
    private final int z;

    public TrapChunk(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public static TrapChunk of(Chunk chunk) {
        World world = chunk.getWorld();
        return new TrapChunk(world.getName(), chunk.getX(), chunk.getZ());
    }

    public String key() {
        return world + ":" + x + ":" + z;
    }

    public String world() {
        return world;
    }

    public int x() {
        return x;
    }

    public int z() {
        return z;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof TrapChunk other)) return false;
        return x == other.x && z == other.z && Objects.equals(world, other.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, z);
    }
}
