package net.novucs.ftop.database;

import net.novucs.ftop.entity.IdentityCache;
import org.bukkit.entity.EntityType;

import java.sql.*;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ChunkSpawnerModel {

    private static final String UPDATE = "UPDATE `chunk_spawner_count` SET `count` = ? WHERE `id` = ?";
    private static final String INSERT = "INSERT INTO `chunk_spawner_count` (`chunk_id`, `spawner_id`, `count`) VALUES(?, ?, ?)";

    private final List<Map.Entry<Integer, Integer>> insertionQueue = new LinkedList<>();
    private final IdentityCache identityCache;
    private final PreparedStatement update;
    private final PreparedStatement insert;

    private ChunkSpawnerModel(IdentityCache identityCache, PreparedStatement update, PreparedStatement insert) {
        this.identityCache = identityCache;
        this.update = update;
        this.insert = insert;
    }

    public static ChunkSpawnerModel of(Connection connection, IdentityCache identityCache) throws SQLException {
        PreparedStatement update = connection.prepareStatement(UPDATE);
        PreparedStatement insert = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
        return new ChunkSpawnerModel(identityCache, update, insert);
    }

    public void executeBatch() throws SQLException {
        // Execute all batched update and insert operations.
        update.executeBatch();
        insert.executeBatch();

        // Add newly created chunk-spawner relations to the identity cache.
        ResultSet resultSet = insert.getGeneratedKeys();

        for (Map.Entry<Integer, Integer> entry : insertionQueue) {
            if (resultSet.next()) {
                int id = resultSet.getInt(1);
                int chunkId = entry.getKey();
                int spawnerId = entry.getValue();
                identityCache.setChunkSpawnerId(chunkId, spawnerId, id);
            }
        }

        resultSet.close();

        insertionQueue.clear();
    }

    public void close() throws SQLException {
        update.close();
        insert.close();
    }

    public void addBatch(int chunkId, Map<EntityType, Integer> spawners) throws SQLException {
        // Persist all spawner counters for this specific chunk worth.
        for (Map.Entry<EntityType, Integer> entry : spawners.entrySet()) {
            EntityType spawner = entry.getKey();
            int count = entry.getValue();
            int spawnerId = identityCache.getSpawnerId(spawner.name());
            addBatch(chunkId, spawnerId, count);
        }
    }

    public void addBatch(int chunkId, int spawnerId, int count) throws SQLException {
        Integer relationId = identityCache.getChunkSpawnerId(chunkId, spawnerId);

        if (relationId == null) {
            insertCounter(chunkId, spawnerId, count);
        } else {
            updateCounter(count, relationId);
        }
    }

    private void insertCounter(int chunkId, int spawnerId, int count) throws SQLException {
        insert.setInt(1, chunkId);
        insert.setInt(2, spawnerId);
        insert.setInt(3, count);
        insert.addBatch();
        insertionQueue.add(new AbstractMap.SimpleImmutableEntry<>(chunkId, spawnerId));
    }

    private void updateCounter(int count, Integer relationId) throws SQLException {
        update.setInt(1, count);
        update.setInt(2, relationId);
        update.addBatch();
    }
}
