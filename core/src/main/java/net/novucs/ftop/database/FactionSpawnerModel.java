package net.novucs.ftop.database;

import net.novucs.ftop.entity.IdentityCache;
import org.bukkit.entity.EntityType;

import java.sql.*;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FactionSpawnerModel {

    private static final String UPDATE = "UPDATE `faction_spawner_count` SET `count` = ? WHERE `id` = ?";
    private static final String INSERT = "INSERT INTO `faction_spawner_count` (`faction_id`, `spawner_id`, `count`) VALUES (?, ?, ?)";

    private final List<Map.Entry<String, Integer>> insertionQueue = new LinkedList<>();
    private final IdentityCache identityCache;
    private final PreparedStatement update;
    private final PreparedStatement insert;

    private FactionSpawnerModel(IdentityCache identityCache, PreparedStatement update, PreparedStatement insert) {
        this.identityCache = identityCache;
        this.update = update;
        this.insert = insert;
    }

    public static FactionSpawnerModel of(Connection connection, IdentityCache identityCache) throws SQLException {
        PreparedStatement update = connection.prepareStatement(UPDATE);
        PreparedStatement insert = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
        return new FactionSpawnerModel(identityCache, update, insert);
    }

    public void executeBatch() throws SQLException {
        // Execute all batched update and insert operations.
        update.executeBatch();
        insert.executeBatch();

        // Add newly created faction-spawner relations to the identity cache.
        ResultSet resultSet = insert.getGeneratedKeys();

        for (Map.Entry<String, Integer> entry : insertionQueue) {
            if (resultSet.next()) {
                int id = resultSet.getInt(1);
                String factionId = entry.getKey();
                int spawnerId = entry.getValue();
                identityCache.setFactionSpawnerId(factionId, spawnerId, id);
            }
        }

        resultSet.close();

        insertionQueue.clear();
    }

    public void close() throws SQLException {
        update.close();
        insert.close();
    }


    public void addBatch(String factionId, Map<EntityType, Integer> spawners) throws SQLException {
        // Persist all spawner counters for this specific faction worth.
        for (Map.Entry<EntityType, Integer> entry : spawners.entrySet()) {
            EntityType spawner = entry.getKey();
            int count = entry.getValue();
            int spawnerId = identityCache.getSpawnerId(spawner.name());
            addBatch(factionId, spawnerId, count);
        }
    }

    public void addBatch(String factionId, int spawnerId, int count) throws SQLException {
        Integer relationId = identityCache.getFactionSpawnerId(factionId, spawnerId);
        Map.Entry<String, Integer> insertionKey = new AbstractMap.SimpleImmutableEntry<>(factionId, spawnerId);

        if (relationId == null) {
            if (!insertionQueue.contains(insertionKey)) {
                insertCounter(factionId, spawnerId, count);
                insertionQueue.add(insertionKey);
            }
        } else {
            updateCounter(count, relationId);
        }
    }

    private void insertCounter(String factionId, int spawnerId, int count) throws SQLException {
        insert.setString(1, factionId);
        insert.setInt(2, spawnerId);
        insert.setInt(3, count);
        insert.addBatch();
    }

    private void updateCounter(int count, Integer relationId) throws SQLException {
        update.setInt(1, count);
        update.setInt(2, relationId);
        update.addBatch();
    }
}
