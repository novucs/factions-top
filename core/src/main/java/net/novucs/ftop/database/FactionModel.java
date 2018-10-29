package net.novucs.ftop.database;

import net.novucs.ftop.entity.FactionWorth;
import net.novucs.ftop.entity.IdentityCache;
import net.novucs.ftop.manager.DatabaseManager;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class FactionModel {

    private static final String UPDATE = "UPDATE `" + DatabaseManager.prefix + "faction` SET `name` = ?, `total_worth` = ?, `total_spawners` = ? WHERE `id` = ?";
    private static final String INSERT = "INSERT INTO `" + DatabaseManager.prefix + "faction` (`id`, `name`, `total_worth`, `total_spawners`) VALUES(?, ?, ?, ?)";
    private static final String DELETE = "DELETE FROM `" + DatabaseManager.prefix + "faction` WHERE `id` = ?";

    private final Connection connection;
    private final IdentityCache identityCache;

    private PreparedStatement update;
    private PreparedStatement insert;
    private PreparedStatement delete;

    public FactionModel(Connection connection, IdentityCache identityCache) {
        this.connection = connection;
        this.identityCache = identityCache;
    }

    public void persist(Set<FactionWorth> factions, Set<String> deletedFactions) throws SQLException {
        init();

        factions.removeIf(factionWorth -> deletedFactions.contains(factionWorth.getFactionId()));

        persistNames(factions);

        persistFactions(factions);

        persistStatistics(factions, deletedFactions);

        deleteFactions(deletedFactions);

        close();
    }

    private void init() throws SQLException {
        delete = connection.prepareStatement(DELETE);
        insert = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
        update = connection.prepareStatement(UPDATE);
    }

    private void close() throws SQLException {
        delete.close();
        insert.close();
        update.close();
    }

    private void deleteFactions(Set<String> factions) throws SQLException {
        for (String factionId : factions) {
            delete.setString(1, factionId);
            delete.addBatch();
            identityCache.removeFaction(factionId);
        }

        delete.executeBatch();
    }

    private void persistNames(Set<FactionWorth> factions) throws SQLException {
        MaterialModel materialModel = MaterialModel.of(connection, identityCache);
        SpawnerModel spawnerModel = SpawnerModel.of(connection, identityCache);
        WorthModel worthModel = WorthModel.of(connection, identityCache);

        for (FactionWorth worth : factions) {
            materialModel.addBatch(worth.getMaterials().keySet());
            spawnerModel.addBatch(worth.getSpawners().keySet());
            worthModel.addBatch(worth.getWorth().keySet());
        }

        materialModel.executeBatch();
        spawnerModel.executeBatch();
        worthModel.executeBatch();

        materialModel.close();
        spawnerModel.close();
        worthModel.close();
    }

    private void persistFactions(Set<FactionWorth> factions) throws SQLException {
        // Insert chunk positions that are not currently in the database.
        Set<FactionWorth> createdFactions = insertFactions(factions);

        // Add newly created chunk positions to the identity cache.
        cacheFactionIds(createdFactions);
    }

    private Set<FactionWorth> insertFactions(Set<FactionWorth> factions) throws SQLException {
        Set<FactionWorth> createdFactions = new HashSet<>();

        for (FactionWorth factionWorth : factions) {
            if (identityCache.hasFaction(factionWorth.getFactionId())) {
                updateFaction(factionWorth);
            } else {
                insertFaction(createdFactions, factionWorth);
            }
        }

        insert.executeBatch();
        update.executeBatch();

        return createdFactions;
    }

    private void updateFaction(FactionWorth faction) throws SQLException {
        update.setString(1, faction.getName());
        update.setDouble(2, faction.getTotalWorth());
        update.setInt(3, faction.getTotalSpawnerCount());
        update.setString(4, faction.getFactionId());
        update.addBatch();
    }

    private void insertFaction(Set<FactionWorth> createdFactions, FactionWorth factionWorth) throws SQLException {
        insert.setString(1, factionWorth.getFactionId());
        insert.setString(2, factionWorth.getName());
        insert.setDouble(3, factionWorth.getTotalWorth());
        insert.setInt(4, factionWorth.getTotalSpawnerCount());
        insert.addBatch();
        createdFactions.add(factionWorth);
    }

    private void cacheFactionIds(Set<FactionWorth> createdFactions) throws SQLException {
        ResultSet resultSet = insert.getGeneratedKeys();

        for (FactionWorth factionWorth : createdFactions) {
            if (resultSet.next()) {
                identityCache.addFaction(factionWorth.getFactionId());
            }
        }

        resultSet.close();
    }

    private void persistStatistics(Set<FactionWorth> factions, Set<String> deletedFactions) throws SQLException {
        FactionMaterialModel materialModel = FactionMaterialModel.of(connection, identityCache);
        FactionSpawnerModel spawnerModel = FactionSpawnerModel.of(connection, identityCache);
        FactionWorthModel worthModel = FactionWorthModel.of(connection, identityCache);

        materialModel.addBatchDelete(deletedFactions);
        spawnerModel.addBatchDelete(deletedFactions);
        worthModel.addBatchDelete(deletedFactions);

        for (FactionWorth faction : factions) {
            materialModel.addBatch(faction.getFactionId(), faction.getMaterials());
            spawnerModel.addBatch(faction.getFactionId(), faction.getSpawners());
            worthModel.addBatch(faction.getFactionId(), faction.getWorth());
        }

        materialModel.executeBatch();
        spawnerModel.executeBatch();
        worthModel.executeBatch();

        materialModel.close();
        spawnerModel.close();
        worthModel.close();
    }
}
