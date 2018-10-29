package net.novucs.ftop.database;

import net.novucs.ftop.entity.IdentityCache;
import net.novucs.ftop.manager.DatabaseManager;
import org.bukkit.Material;

import java.sql.*;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ChunkMaterialModel {

    private static final String UPDATE = "UPDATE `" + DatabaseManager.prefix + "chunk_material_count` SET `count` = ? WHERE `id` = ?";
    private static final String INSERT = "INSERT INTO `" + DatabaseManager.prefix + "chunk_material_count` (`chunk_id`, `material_id`, `count`) VALUES(?, ?, ?)";

    private final List<Map.Entry<Integer, Integer>> insertionQueue = new LinkedList<>();
    private final IdentityCache identityCache;
    private final PreparedStatement update;
    private final PreparedStatement insert;

    private ChunkMaterialModel(IdentityCache identityCache, PreparedStatement update, PreparedStatement insert) {
        this.identityCache = identityCache;
        this.update = update;
        this.insert = insert;
    }

    public static ChunkMaterialModel of(Connection connection, IdentityCache identityCache) throws SQLException {
        PreparedStatement update = connection.prepareStatement(UPDATE);
        PreparedStatement insert = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
        return new ChunkMaterialModel(identityCache, update, insert);
    }

    public void executeBatch() throws SQLException {
        // Execute all batched update and insert operations.
        update.executeBatch();
        insert.executeBatch();

        // Add newly created chunk-material relations to the identity cache.
        ResultSet resultSet = insert.getGeneratedKeys();

        for (Map.Entry<Integer, Integer> entry : insertionQueue) {
            if (resultSet.next()) {
                int id = resultSet.getInt(1);
                int chunkId = entry.getKey();
                int materialId = entry.getValue();
                identityCache.setChunkMaterialId(chunkId, materialId, id);
            }
        }

        resultSet.close();

        insertionQueue.clear();
    }

    public void close() throws SQLException {
        update.close();
        insert.close();
    }

    public void addBatch(int chunkId, Map<Material, Integer> materials) throws SQLException {
        // Persist all material counters for this specific chunk worth.
        for (Map.Entry<Material, Integer> entry : materials.entrySet()) {
            Material material = entry.getKey();
            int count = entry.getValue();
            int materialId = identityCache.getMaterialId(material.name());
            addBatch(chunkId, materialId, count);
        }
    }

    public void addBatch(int chunkId, int materialId, int count) throws SQLException {
        Integer relationId = identityCache.getChunkMaterialId(chunkId, materialId);
        Map.Entry<Integer, Integer> insertionKey = new AbstractMap.SimpleImmutableEntry<>(chunkId, materialId);

        if (relationId == null) {
            if (!insertionQueue.contains(insertionKey)) {
                insertCounter(chunkId, materialId, count);
                insertionQueue.add(insertionKey);
            }
        } else {
            updateCounter(count, relationId);
        }
    }

    private void insertCounter(int chunkId, int materialId, int count) throws SQLException {
        insert.setInt(1, chunkId);
        insert.setInt(2, materialId);
        insert.setInt(3, count);
        insert.addBatch();
    }

    private void updateCounter(int count, Integer relationId) throws SQLException {
        update.setInt(1, count);
        update.setInt(2, relationId);
        update.addBatch();
    }
}
