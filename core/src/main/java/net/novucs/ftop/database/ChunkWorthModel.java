package net.novucs.ftop.database;

import net.novucs.ftop.WorthType;
import net.novucs.ftop.entity.IdentityCache;
import net.novucs.ftop.manager.DatabaseManager;

import java.sql.*;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ChunkWorthModel {

    private static final String UPDATE = "UPDATE `" + DatabaseManager.prefix + "chunk_worth` SET `worth` = ? WHERE `id` = ?";
    private static final String INSERT = "INSERT INTO `" + DatabaseManager.prefix + "chunk_worth` (`chunk_id`, `worth_id`, `worth`) VALUES(?, ?, ?)";

    private final List<Map.Entry<Integer, Integer>> insertionQueue = new LinkedList<>();
    private final IdentityCache identityCache;
    private final PreparedStatement update;
    private final PreparedStatement insert;

    private ChunkWorthModel(IdentityCache identityCache, PreparedStatement update, PreparedStatement insert) {
        this.identityCache = identityCache;
        this.update = update;
        this.insert = insert;
    }

    public static ChunkWorthModel of(Connection connection, IdentityCache identityCache) throws SQLException {
        PreparedStatement update = connection.prepareStatement(UPDATE);
        PreparedStatement insert = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
        return new ChunkWorthModel(identityCache, update, insert);
    }

    public void executeBatch() throws SQLException {
        // Execute all batched update and insert operations.
        update.executeBatch();
        insert.executeBatch();

        // Add newly created chunk-worth relations to the identity cache.
        ResultSet resultSet = insert.getGeneratedKeys();

        for (Map.Entry<Integer, Integer> entry : insertionQueue) {
            if (resultSet.next()) {
                int id = resultSet.getInt(1);
                int chunkId = entry.getKey();
                int worthId = entry.getValue();
                identityCache.setChunkWorthId(chunkId, worthId, id);
            }
        }

        resultSet.close();

        insertionQueue.clear();
    }

    public void close() throws SQLException {
        update.close();
        insert.close();
    }

    public void addBatch(int chunkId, Map<WorthType, Double> worth) throws SQLException {
        // Persist all worth values for this specific chunk worth.
        for (Map.Entry<WorthType, Double> entry : worth.entrySet()) {
            WorthType worthType = entry.getKey();
            double value = entry.getValue();
            int worthTypeId = identityCache.getWorthId(worthType.name());
            addBatch(chunkId, worthTypeId, value);
        }
    }

    public void addBatch(int chunkId, int worthTypeId, double value) throws SQLException {
        Integer relationId = identityCache.getChunkWorthId(chunkId, worthTypeId);
        Map.Entry<Integer, Integer> insertionKey = new AbstractMap.SimpleImmutableEntry<>(chunkId, worthTypeId);

        if (relationId == null) {
            if (!insertionQueue.contains(insertionKey)) {
                insertCounter(chunkId, worthTypeId, value);
                insertionQueue.add(insertionKey);
            }
        } else {
            updateCounter(value, relationId);
        }
    }

    private void insertCounter(int chunkId, int worthId, double value) throws SQLException {
        insert.setInt(1, chunkId);
        insert.setInt(2, worthId);
        insert.setDouble(3, value);
        insert.addBatch();
    }

    private void updateCounter(double value, Integer relationId) throws SQLException {
        update.setDouble(1, value);
        update.setInt(2, relationId);
        update.addBatch();
    }
}
