package net.novucs.ftop.database;

import net.novucs.ftop.WorthType;
import net.novucs.ftop.entity.IdentityCache;

import java.sql.*;
import java.util.*;

public class FactionWorthModel {

    private static final String UPDATE = "UPDATE `faction_worth` SET `worth` = ? WHERE `id` = ?";
    private static final String INSERT = "INSERT INTO `faction_worth` (`faction_id`, `worth_id`, `worth`) VALUES (?, ?, ?)";
    private static final String DELETE = "DELETE FROM `faction_worth` WHERE `faction_id` = ?";

    private final List<Map.Entry<String, Integer>> insertionQueue = new LinkedList<>();
    private final IdentityCache identityCache;
    private final PreparedStatement update;
    private final PreparedStatement insert;
    private final PreparedStatement delete;

    public FactionWorthModel(IdentityCache identityCache, PreparedStatement update, PreparedStatement insert, PreparedStatement delete) {
        this.identityCache = identityCache;
        this.update = update;
        this.insert = insert;
        this.delete = delete;
    }

    public static FactionWorthModel of(Connection connection, IdentityCache identityCache) throws SQLException {
        PreparedStatement update = connection.prepareStatement(UPDATE);
        PreparedStatement insert = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
        PreparedStatement delete = connection.prepareStatement(DELETE);
        return new FactionWorthModel(identityCache, update, insert, delete);
    }

    public void executeBatch() throws SQLException {
        // Execute all batched update and insert operations.
        update.executeBatch();
        insert.executeBatch();
        delete.executeBatch();

        // Add newly created faction-spawner relations to the identity cache.
        ResultSet resultSet = insert.getGeneratedKeys();

        for (Map.Entry<String, Integer> entry : insertionQueue) {
            if (resultSet.next()) {
                int id = resultSet.getInt(1);
                String factionId = entry.getKey();
                int worthId = entry.getValue();
                identityCache.setFactionWorthId(factionId, worthId, id);
            }
        }

        resultSet.close();

        insertionQueue.clear();
    }

    public void close() throws SQLException {
        delete.close();
        update.close();
        insert.close();
    }

    public void addBatch(String factionId, Map<WorthType, Double> worthTypes) throws SQLException {
        // Persist all spawner counters for this specific faction worth.
        for (Map.Entry<WorthType, Double> entry : worthTypes.entrySet()) {
            WorthType worthType = entry.getKey();
            double worth = entry.getValue();
            int worthId = identityCache.getWorthId(worthType.name());
            addBatch(factionId, worthId, worth);
        }
    }

    public void addBatch(String factionId, int worthId, double worth) throws SQLException {
        Integer relationId = identityCache.getFactionWorthId(factionId, worthId);
        Map.Entry<String, Integer> insertionKey = new AbstractMap.SimpleImmutableEntry<>(factionId, worthId);

        if (relationId == null) {
            if (!insertionQueue.contains(insertionKey)) {
                insertCounter(factionId, worthId, worth);
                insertionQueue.add(insertionKey);
            }
        } else {
            updateCounter(worth, relationId);
        }
    }

    private void insertCounter(String factionId, int worthId, double worth) throws SQLException {
        insert.setString(1, factionId);
        insert.setInt(2, worthId);
        insert.setDouble(3, worth);
        insert.addBatch();
    }

    private void updateCounter(double worth, Integer relationId) throws SQLException {
        update.setDouble(1, worth);
        update.setInt(2, relationId);
        update.addBatch();
    }

    public void addBatchDelete(Collection<String> factions) throws SQLException {
        for (String factionId : factions) {
            addBatchDelete(factionId);
        }
    }

    public void addBatchDelete(String factionId) throws SQLException {
        delete.setString(1, factionId);
        delete.addBatch();
    }
}
