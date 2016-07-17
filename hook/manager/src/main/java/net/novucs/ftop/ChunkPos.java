package net.novucs.ftop;

import org.bukkit.ChunkSnapshot;
import org.bukkit.block.Block;

import java.util.Objects;

public class ChunkPos {

    private final String world;
    private final int x;
    private final int z;

    public static ChunkPos of(Block block) {
        return new ChunkPos(block.getWorld().getName(), block.getX() >> 4, block.getZ() >> 4);
    }

    public static ChunkPos of(ChunkSnapshot snapshot) {
        return new ChunkPos(snapshot.getWorldName(), snapshot.getX(), snapshot.getZ());
    }

    public static ChunkPos of(String world, int x, int z) {
        return new ChunkPos(world, x, z);
    }

    private ChunkPos(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkPos chunkPos = (ChunkPos) o;
        return x == chunkPos.x &&
                z == chunkPos.z &&
                Objects.equals(world, chunkPos.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, z);
    }

    @Override
    public String toString() {
        return "ChunkPos{" +
                "world='" + world + '\'' +
                ", x=" + x +
                ", z=" + z +
                '}';
    }
}
