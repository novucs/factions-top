package net.novucs.ftop.database;

import net.novucs.ftop.entity.BlockPos;
import net.novucs.ftop.entity.IdentityCache;

import java.sql.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class BlockModel {

    private static final String INSERT = "INSERT INTO `block` (`world_id`, `x`, `y`, `z`) VALUES(?, ?, ?, ?)";

    private final List<FormattedBlockPos> insertionQueue = new LinkedList<>();
    private final IdentityCache identityCache;
    private final PreparedStatement insert;

    private BlockModel(IdentityCache identityCache, PreparedStatement insert) {
        this.identityCache = identityCache;
        this.insert = insert;
    }

    public static BlockModel of(Connection connection, IdentityCache identityCache) throws SQLException {
        PreparedStatement insert = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
        return new BlockModel(identityCache, insert);
    }

    public void executeBatch() throws SQLException {
        // Execute all batched update and insert operations.
        insert.executeBatch();

        // Add newly created blocks to the identity cache.
        ResultSet resultSet = insert.getGeneratedKeys();

        for (FormattedBlockPos block : insertionQueue) {
            if (resultSet.next()) {
                int id = resultSet.getInt(1);
                int worldId = block.getWorldId();
                int x = block.getX();
                int y = block.getY();
                int z = block.getZ();
                identityCache.setBlockId(worldId, x, y, z, id);
            }
        }

        resultSet.close();

        insertionQueue.clear();
    }

    public void close() throws SQLException {
        insert.close();
    }

    public void addBatch(Collection<BlockPos> blocks) throws SQLException {
        for (BlockPos block : blocks) {
            addBatch(block);
        }
    }

    public void addBatch(BlockPos block) throws SQLException {
        int worldId = identityCache.getWorldId(block.getWorld());
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        addBatch(worldId, x, y, z);
    }

    public void addBatch(int worldId, int x, int y, int z) throws SQLException {
        FormattedBlockPos block = new FormattedBlockPos(worldId, x, y, z);

        if (identityCache.hasBlock(worldId, x, y, z) || insertionQueue.contains(block)) {
            return;
        }

        insert.setInt(1, worldId);
        insert.setInt(2, x);
        insert.setInt(3, y);
        insert.setInt(4, z);
        insert.addBatch();
        insertionQueue.add(block);
    }

    public class FormattedBlockPos {
        private final int worldId;
        private final int x;
        private final int y;
        private final int z;

        public FormattedBlockPos(int worldId, int x, int y, int z) {
            this.worldId = worldId;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public int getWorldId() {
            return worldId;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FormattedBlockPos block = (FormattedBlockPos) o;
            return worldId == block.worldId &&
                    x == block.x &&
                    y == block.y &&
                    z == block.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(worldId, x, y, z);
        }
    }
}
