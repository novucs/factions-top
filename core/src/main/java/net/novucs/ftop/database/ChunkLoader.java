package net.novucs.ftop.database;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.novucs.ftop.WorthType;
import net.novucs.ftop.entity.ChunkPos;
import net.novucs.ftop.entity.ChunkWorth;
import net.novucs.ftop.entity.IdentityCache;
import net.novucs.ftop.manager.DatabaseManager;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ChunkLoader {

    private static final String SELECT_CHUNK = "SELECT * FROM `" + DatabaseManager.prefix + "chunk`";
    private static final String SELECT_CHUNK_MATERIAL = "SELECT * FROM `" + DatabaseManager.prefix + "chunk_material_count`";
    private static final String SELECT_CHUNK_SPAWNER = "SELECT * FROM `" + DatabaseManager.prefix + "chunk_spawner_count`";
    private static final String SELECT_CHUNK_WORTH = "SELECT * FROM `" + DatabaseManager.prefix + "chunk_worth`";

    private final IdentityCache identityCache;
    private final PreparedStatement selectChunk;
    private final PreparedStatement selectChunkMaterial;
    private final PreparedStatement selectChunkSpawner;
    private final PreparedStatement selectChunkWorth;

    private ChunkLoader(IdentityCache identityCache,
                        PreparedStatement selectChunk,
                        PreparedStatement selectChunkMaterial,
                        PreparedStatement selectChunkSpawner,
                        PreparedStatement selectChunkWorth) {
        this.identityCache = identityCache;
        this.selectChunk = selectChunk;
        this.selectChunkMaterial = selectChunkMaterial;
        this.selectChunkSpawner = selectChunkSpawner;
        this.selectChunkWorth = selectChunkWorth;
    }

    public static ChunkLoader of(Connection connection, IdentityCache identityCache) throws SQLException {
        PreparedStatement selectChunk = connection.prepareStatement(SELECT_CHUNK);
        PreparedStatement selectChunkMaterial = connection.prepareStatement(SELECT_CHUNK_MATERIAL);
        PreparedStatement selectChunkSpawner = connection.prepareStatement(SELECT_CHUNK_SPAWNER);
        PreparedStatement selectChunkWorth = connection.prepareStatement(SELECT_CHUNK_WORTH);
        return new ChunkLoader(identityCache, selectChunk, selectChunkMaterial, selectChunkSpawner, selectChunkWorth);
    }

    public Map<ChunkPos, ChunkWorth> load() throws SQLException {
        Map<ChunkPos, ChunkWorth> target = new HashMap<>();
        Map<Integer, ChunkPos> chunks = loadChunk();
        Table<Integer, Material, Integer> globalMaterialCount = loadChunkMaterial();
        Table<Integer, EntityType, Integer> globalSpawnerCount = loadChunkSpawner();
        Table<Integer, WorthType, Double> globalWorth = loadChunkWorth();

        for (Map.Entry<Integer, ChunkPos> entry : chunks.entrySet()) {
            int chunkId = entry.getKey();

            Map<Material, Integer> chunkMaterialCount = new EnumMap<>(Material.class);
            chunkMaterialCount.putAll(globalMaterialCount.row(chunkId));

            Map<EntityType, Integer> chunkSpawnerCount = new EnumMap<>(EntityType.class);
            chunkSpawnerCount.putAll(globalSpawnerCount.row(chunkId));

            Map<WorthType, Double> chunkWorth = new EnumMap<>(WorthType.class);
            chunkWorth.putAll(globalWorth.row(chunkId));

            ChunkPos chunk = entry.getValue();
            ChunkWorth worth = new ChunkWorth(chunkWorth, chunkMaterialCount, chunkSpawnerCount);

            target.put(chunk, worth);
        }

        return target;
    }

    public void close() throws SQLException {
        selectChunk.close();
        selectChunkMaterial.close();
        selectChunkSpawner.close();
        selectChunkWorth.close();
    }

    private Map<Integer, ChunkPos> loadChunk() throws SQLException {
        Map<Integer, ChunkPos> target = new HashMap<>();
        ResultSet resultSet = selectChunk.executeQuery();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            int worldId = resultSet.getInt("world_id");
            int x = resultSet.getInt("x");
            int z = resultSet.getInt("z");

            identityCache.setChunkPosId(worldId, x, z, id);
            identityCache.getWorldName(worldId).ifPresent((worldName) ->
                    target.put(id, ChunkPos.of(worldName, x, z)));
        }

        resultSet.close();
        return target;
    }

    private Table<Integer, Material, Integer> loadChunkMaterial() throws SQLException {
        Table<Integer, Material, Integer> target = HashBasedTable.create();
        ResultSet resultSet = selectChunkMaterial.executeQuery();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            int chunkId = resultSet.getInt("chunk_id");
            int materialId = resultSet.getInt("material_id");
            int count = resultSet.getInt("count");

            identityCache.setChunkMaterialId(chunkId, materialId, id);
            identityCache.getMaterial(materialId).ifPresent(material ->
                    target.put(chunkId, material, count));
        }

        resultSet.close();
        return target;
    }

    private Table<Integer, EntityType, Integer> loadChunkSpawner() throws SQLException {
        Table<Integer, EntityType, Integer> target = HashBasedTable.create();
        ResultSet resultSet = selectChunkSpawner.executeQuery();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            int chunkId = resultSet.getInt("chunk_id");
            int spawnerId = resultSet.getInt("spawner_id");
            int count = resultSet.getInt("count");

            identityCache.setChunkSpawnerId(chunkId, spawnerId, id);
            identityCache.getSpawner(spawnerId).ifPresent(spawner ->
                    target.put(chunkId, spawner, count));
        }

        resultSet.close();
        return target;
    }

    private Table<Integer, WorthType, Double> loadChunkWorth() throws SQLException {
        Table<Integer, WorthType, Double> target = HashBasedTable.create();
        ResultSet resultSet = selectChunkWorth.executeQuery();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            int chunkId = resultSet.getInt("chunk_id");
            int worthId = resultSet.getInt("worth_id");
            double worth = resultSet.getDouble("worth");

            identityCache.setChunkWorthId(chunkId, worthId, id);
            identityCache.getWorthType(worthId).ifPresent(worthType ->
                    target.put(chunkId, worthType, worth));
        }

        resultSet.close();
        return target;
    }
}
