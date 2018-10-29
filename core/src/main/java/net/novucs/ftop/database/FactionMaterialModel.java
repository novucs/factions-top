package net.novucs.ftop.database;

import net.novucs.ftop.entity.IdentityCache;
import net.novucs.ftop.manager.DatabaseManager;
import org.bukkit.Material;

import java.sql.*;
import java.util.*;

public class FactionMaterialModel {

    private static final String UPDATE = "UPDATE `" + DatabaseManager.prefix + "faction_material_count` SET `count` = ? WHERE `id` = ?";
    private static final String INSERT = "INSERT INTO `" + DatabaseManager.prefix + "faction_material_count` (`faction_id`, `material_id`, `count`) VALUES (?, ?, ?)";
    private static final String DELETE = "DELETE FROM `" + DatabaseManager.prefix + "faction_material_count` WHERE `faction_id` = ?";

    private final List<Map.Entry<String, Integer>> insertionQueue = new LinkedList<>();
    private final IdentityCache identityCache;
    private final PreparedStatement update;
    private final PreparedStatement insert;
    private final PreparedStatement delete;

    private FactionMaterialModel(IdentityCache identityCache, PreparedStatement update, PreparedStatement insert, PreparedStatement delete) {
        this.identityCache = identityCache;
        this.update = update;
        this.insert = insert;
        this.delete = delete;
    }

    public static FactionMaterialModel of(Connection connection, IdentityCache identityCache) throws SQLException {
        PreparedStatement update = connection.prepareStatement(UPDATE);
        PreparedStatement insert = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
        PreparedStatement delete = connection.prepareStatement(DELETE);
        return new FactionMaterialModel(identityCache, update, insert, delete);
    }

    public void executeBatch() throws SQLException {
        // Execute all batched update and insert operations.
        update.executeBatch();
        insert.executeBatch();
        delete.executeBatch();

        // Add newly created faction-material relations to the identity cache.
        ResultSet resultSet = insert.getGeneratedKeys();

        for (Map.Entry<String, Integer> entry : insertionQueue) {
            if (resultSet.next()) {
                int id = resultSet.getInt(1);
                String factionId = entry.getKey();
                int materialId = entry.getValue();
                identityCache.setFactionMaterialId(factionId, materialId, id);
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

    public void addBatch(String factionId, Map<Material, Integer> materials) throws SQLException {
        // Persist all material counters for this specific faction worth.
        for (Map.Entry<Material, Integer> entry : materials.entrySet()) {
            Material material = entry.getKey();
            int count = entry.getValue();
            int materialId = identityCache.getMaterialId(material.name());
            addBatch(factionId, materialId, count);
        }
    }

    public void addBatch(String factionId, int materialId, int count) throws SQLException {
        Integer relationId = identityCache.getFactionMaterialId(factionId, materialId);
        Map.Entry<String, Integer> insertionKey = new AbstractMap.SimpleImmutableEntry<>(factionId, materialId);

        if (relationId == null) {
            if (!insertionQueue.contains(insertionKey)) {
                insertCounter(factionId, materialId, count);
                insertionQueue.add(insertionKey);
            }
        } else {
            updateCounter(count, relationId);
        }
    }

    private void insertCounter(String factionId, int materialId, int count) throws SQLException {
        insert.setString(1, factionId);
        insert.setInt(2, materialId);
        insert.setInt(3, count);
        insert.addBatch();
    }

    private void updateCounter(int count, Integer relationId) throws SQLException {
        update.setInt(1, count);
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
