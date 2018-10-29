package net.novucs.ftop.database;

import net.novucs.ftop.entity.ChunkPos;
import net.novucs.ftop.entity.ChunkWorth;
import net.novucs.ftop.entity.IdentityCache;
import net.novucs.ftop.manager.DatabaseManager;

import java.sql.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChunkModel {

    private static final String INSERT = "INSERT INTO `" + DatabaseManager.prefix + "chunk` (`world_id`, `x`, `z`) VALUES(?, ?, ?)";

    private final Connection connection;
    private final IdentityCache identityCache;

    private PreparedStatement insert;

    public ChunkModel(Connection connection, IdentityCache identityCache) {
        this.connection = connection;
        this.identityCache = identityCache;
    }

    public void persist(Set<Map.Entry<ChunkPos, ChunkWorth>> chunks) throws SQLException {
        init();

        persistNames(chunks);

        persistPositions(chunks);

        persistStatistics(chunks);

        close();
    }

    private void init() throws SQLException {
        insert = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
    }

    private void close() throws SQLException {
        insert.close();
    }

    private void persistNames(Set<Map.Entry<ChunkPos, ChunkWorth>> chunks) throws SQLException {
        MaterialModel materialModel = MaterialModel.of(connection, identityCache);
        SpawnerModel spawnerModel = SpawnerModel.of(connection, identityCache);
        WorldModel worldModel = WorldModel.of(connection, identityCache);
        WorthModel worthModel = WorthModel.of(connection, identityCache);

        for (Map.Entry<ChunkPos, ChunkWorth> entry : chunks) {
            ChunkPos position = entry.getKey();
            ChunkWorth worth = entry.getValue();

            materialModel.addBatch(worth.getMaterials().keySet());
            spawnerModel.addBatch(worth.getSpawners().keySet());
            worldModel.addBatch(position.getWorld());
            worthModel.addBatch(worth.getWorth().keySet());
        }

        materialModel.executeBatch();
        spawnerModel.executeBatch();
        worldModel.executeBatch();
        worthModel.executeBatch();

        materialModel.close();
        spawnerModel.close();
        worldModel.close();
        worthModel.close();
    }

    private void persistPositions(Set<Map.Entry<ChunkPos, ChunkWorth>> chunks) throws SQLException {
        // Insert chunk positions that are not currently in the database.
        Set<Map.Entry<ChunkPos, ChunkWorth>> createdChunks = insertChunkPositions(chunks);

        // Add newly created chunk positions to the identity cache.
        cacheChunkIds(createdChunks);
    }

    private Set<Map.Entry<ChunkPos, ChunkWorth>> insertChunkPositions(Set<Map.Entry<ChunkPos, ChunkWorth>> chunks) throws SQLException {
        Set<Map.Entry<ChunkPos, ChunkWorth>> createdChunks = new HashSet<>();

        for (Map.Entry<ChunkPos, ChunkWorth> entry : chunks) {
            ChunkPos position = entry.getKey();
            int worldId = identityCache.getWorldId(position.getWorld());

            if (identityCache.hasChunkPos(worldId, position.getX(), position.getZ())) {
                continue;
            }

            insert.setInt(1, worldId);
            insert.setInt(2, position.getX());
            insert.setInt(3, position.getZ());
            insert.addBatch();
            createdChunks.add(entry);
        }

        insert.executeBatch();

        return createdChunks;
    }

    private void cacheChunkIds(Set<Map.Entry<ChunkPos, ChunkWorth>> createdChunks) throws SQLException {
        ResultSet resultSet = insert.getGeneratedKeys();

        for (Map.Entry<ChunkPos, ChunkWorth> entry : createdChunks) {
            ChunkPos position = entry.getKey();
            int worldId = identityCache.getWorldId(position.getWorld());

            if (resultSet.next()) {
                int id = resultSet.getInt(1);
                identityCache.setChunkPosId(worldId, position.getX(), position.getZ(), id);
            }
        }

        resultSet.close();
    }

    private void persistStatistics(Set<Map.Entry<ChunkPos, ChunkWorth>> chunks) throws SQLException {
        ChunkMaterialModel chunkMaterialModel = ChunkMaterialModel.of(connection, identityCache);
        ChunkSpawnerModel chunkSpawnerModel = ChunkSpawnerModel.of(connection, identityCache);
        ChunkWorthModel chunkWorthModel = ChunkWorthModel.of(connection, identityCache);

        for (Map.Entry<ChunkPos, ChunkWorth> entry : chunks) {
            ChunkPos position = entry.getKey();
            ChunkWorth worth = entry.getValue();

            int worldId = identityCache.getWorldId(position.getWorld());
            int chunkId = identityCache.getChunkPosId(worldId, position.getX(), position.getZ());

            chunkMaterialModel.addBatch(chunkId, worth.getMaterials());
            chunkSpawnerModel.addBatch(chunkId, worth.getSpawners());
            chunkWorthModel.addBatch(chunkId, worth.getWorth());
        }

        chunkMaterialModel.executeBatch();
        chunkSpawnerModel.executeBatch();
        chunkWorthModel.executeBatch();

        chunkMaterialModel.close();
        chunkSpawnerModel.close();
        chunkWorthModel.close();
    }
}
