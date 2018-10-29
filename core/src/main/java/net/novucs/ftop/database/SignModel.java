package net.novucs.ftop.database;

import net.novucs.ftop.entity.BlockPos;
import net.novucs.ftop.entity.IdentityCache;
import net.novucs.ftop.manager.DatabaseManager;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SignModel {

    private static final String UPDATE = "UPDATE `" + DatabaseManager.prefix + "sign` SET `rank` = ? WHERE `id` = ?";
    private static final String INSERT = "INSERT INTO `" + DatabaseManager.prefix + "sign` (`block_id`, `rank`) VALUES(?, ?)";
    private static final String DELETE = "DELETE FROM `" + DatabaseManager.prefix + "sign` WHERE `id` = ?";

    private final List<Integer> insertionQueue = new LinkedList<>();
    private final IdentityCache identityCache;
    private final PreparedStatement update;
    private final PreparedStatement insert;
    private final PreparedStatement delete;

    private SignModel(IdentityCache identityCache, PreparedStatement update, PreparedStatement insert,
                      PreparedStatement delete) {
        this.identityCache = identityCache;
        this.update = update;
        this.insert = insert;
        this.delete = delete;
    }

    public static SignModel of(Connection connection, IdentityCache identityCache) throws SQLException {
        PreparedStatement update = connection.prepareStatement(UPDATE);
        PreparedStatement insert = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
        PreparedStatement delete = connection.prepareStatement(DELETE);
        return new SignModel(identityCache, update, insert, delete);
    }

    public void executeBatch() throws SQLException {
        // Execute all batched update and insert operations.
        delete.executeBatch();
        update.executeBatch();
        insert.executeBatch();

        // Add newly created signs to the identity cache.
        ResultSet resultSet = insert.getGeneratedKeys();

        for (Integer blockId : insertionQueue) {
            if (resultSet.next()) {
                int id = resultSet.getInt(1);
                identityCache.setSignId(blockId, id);
            }
        }

        resultSet.close();

        insertionQueue.clear();
    }

    public void close() throws SQLException {
        update.close();
        insert.close();
        delete.close();
    }

    public void addBatch(Set<Map.Entry<BlockPos, Integer>> signs) throws SQLException {
        // Persist all material counters for this specific chunk worth.
        for (Map.Entry<BlockPos, Integer> entry : signs) {
            BlockPos block = entry.getKey();
            int worldId = identityCache.getWorldId(block.getWorld());
            int blockId = identityCache.getBlockId(worldId, block.getX(), block.getY(), block.getZ());
            int rank = entry.getValue();
            addBatch(blockId, rank);
        }
    }

    public void addBatch(int blockId, int rank) throws SQLException {
        Integer id = identityCache.getSignId(blockId);

        if (id == null) {
            insertSign(blockId, rank);
        } else {
            updateSign(id, rank);
        }
    }

    private void insertSign(int blockId, int rank) throws SQLException {
        insert.setInt(1, blockId);
        insert.setInt(2, rank);
        insert.addBatch();
        insertionQueue.add(blockId);
    }

    private void updateSign(int id, int rank) throws SQLException {
        update.setInt(1, rank);
        update.setInt(2, id);
        update.addBatch();
    }

    public void addBatchDelete(Set<BlockPos> signs) throws SQLException {
        for (BlockPos block : signs) {
            int worldId = identityCache.getWorldId(block.getWorld());
            int blockId = identityCache.getBlockId(worldId, block.getX(), block.getY(), block.getZ());
            addBatchDelete(blockId);
        }
    }

    public void addBatchDelete(int blockId) throws SQLException {
        Integer signId = identityCache.getSignId(blockId);

        if (signId == null) {
            return;
        }

        delete.setInt(1, signId);
        delete.addBatch();
        identityCache.setSignId(blockId, null);
    }
}
