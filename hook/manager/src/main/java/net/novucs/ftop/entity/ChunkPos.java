package net.novucs.ftop.entity;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Server;

import java.util.Objects;

public class ChunkPos {

    private final String world;
    private final int x;
    private final int z;

    public static ChunkPos of(Chunk chunk) {
        return new ChunkPos(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
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

    public Chunk getChunk(Server server) {
        if (server.getWorld(world) == null) return null;
        return server.getWorld(world).getChunkAt(x, z);
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
