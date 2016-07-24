package net.novucs.ftop;

import org.bukkit.Server;
import org.bukkit.block.Block;

import java.util.Objects;

public class BlockPos {

    private final String world;
    private final int x;
    private final int y;
    private final int z;

    public static BlockPos of(Block block) {
        return new BlockPos(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    public static BlockPos of(String world, int x, int y, int z) {
        return new BlockPos(world, x, y, z);
    }

    private BlockPos(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Block getBlock(Server server) {
        if (server.getWorld(world) == null) return null;
        return server.getWorld(world).getBlockAt(x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockPos blockPos = (BlockPos) o;
        return x == blockPos.x &&
                Double.compare(blockPos.y, y) == 0 &&
                Double.compare(blockPos.z, z) == 0 &&
                Objects.equals(world, blockPos.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z);
    }

    @Override
    public String toString() {
        return "BlockPos{" +
                "world='" + world + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
