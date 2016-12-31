package net.novucs.ftop.database;

import net.novucs.ftop.entity.IdentityCache;
import org.bukkit.Material;

import java.sql.*;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FactionMaterialModel {

    private static final String UPDATE = "UPDATE `faction_material_count` SET `count` = ? WHERE `id` = ?";
    private static final String INSERT = "INSERT INTO `faction_material_count` (`faction_id`, `material_id`, `count`) VALUES (?, ?, ?)";

    private final List<Map.Entry<String, Integer>> insertionQueue = new LinkedList<>();
    private final IdentityCache identityCache;
    private final PreparedStatement update;
    private final PreparedStatement insert;

    private FactionMaterialModel(IdentityCache identityCache, PreparedStatement update, PreparedStatement insert) {
        this.identityCache = identityCache;
        this.update = update;
        this.insert = insert;
    }

    public static FactionMaterialModel of(Connection connection, IdentityCache identityCache) throws SQLException {
        PreparedStatement update = connection.prepareStatement(UPDATE);
        PreparedStatement insert = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
        return new FactionMaterialModel(identityCache, update, insert);
    }

    public void executeBatch() throws SQLException {
        // Execute all batched update and insert operations.
        update.executeBatch();
        insert.executeBatch();

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

        if (relationId == null) {
            insertCounter(factionId, materialId, count);
        } else {
            updateCounter(count, relationId);
        }
    }

    private void insertCounter(String factionId, int materialId, int count) throws SQLException {
        insert.setString(1, factionId);
        insert.setInt(2, materialId);
        insert.setInt(3, count);
        insert.addBatch();
        insertionQueue.add(new AbstractMap.SimpleImmutableEntry<>(factionId, materialId));
    }

    private void updateCounter(int count, Integer relationId) throws SQLException {
        update.setInt(1, count);
        update.setInt(2, relationId);
        update.addBatch();
    }
}
