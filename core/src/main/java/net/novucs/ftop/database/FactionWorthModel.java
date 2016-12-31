package net.novucs.ftop.database;

import net.novucs.ftop.WorthType;
import net.novucs.ftop.entity.IdentityCache;

import java.sql.*;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FactionWorthModel {

    private static final String UPDATE = "UPDATE `faction_worth` SET `worth` = ? WHERE `id` = ?";
    private static final String INSERT = "INSERT INTO `faction_worth` (`faction_id`, `worth_id`, `worth`) VALUES (?, ?, ?)";

    private final List<Map.Entry<String, Integer>> insertionQueue = new LinkedList<>();
    private final IdentityCache identityCache;
    private final PreparedStatement update;
    private final PreparedStatement insert;

    private FactionWorthModel(IdentityCache identityCache, PreparedStatement update, PreparedStatement insert) {
        this.identityCache = identityCache;
        this.update = update;
        this.insert = insert;
    }

    public static FactionWorthModel of(Connection connection, IdentityCache identityCache) throws SQLException {
        PreparedStatement update = connection.prepareStatement(UPDATE);
        PreparedStatement insert = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
        return new FactionWorthModel(identityCache, update, insert);
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
                int worthId = entry.getValue();
                identityCache.setFactionWorthId(factionId, worthId, id);
            }
        }

        resultSet.close();

        insertionQueue.clear();
    }

    public void close() throws SQLException {
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
}
