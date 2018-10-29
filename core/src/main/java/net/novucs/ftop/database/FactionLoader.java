package net.novucs.ftop.database;

import net.novucs.ftop.entity.IdentityCache;
import net.novucs.ftop.manager.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FactionLoader {

    private static final String SELECT_FACTION = "SELECT `id` FROM `" + DatabaseManager.prefix + "faction`";
    private static final String SELECT_FACTION_MATERIAL = "SELECT * FROM `" + DatabaseManager.prefix + "faction_material_count`";
    private static final String SELECT_FACTION_SPAWNER = "SELECT * FROM `" + DatabaseManager.prefix + "faction_spawner_count`";
    private static final String SELECT_FACTION_WORTH = "SELECT * FROM `" + DatabaseManager.prefix + "faction_worth`";

    private final IdentityCache identityCache;
    private final PreparedStatement selectFaction;
    private final PreparedStatement selectFactionMaterial;
    private final PreparedStatement selectFactionSpawner;
    private final PreparedStatement selectFactionWorth;

    private FactionLoader(IdentityCache identityCache,
                          PreparedStatement selectFaction,
                          PreparedStatement selectFactionMaterial,
                          PreparedStatement selectFactionSpawner,
                          PreparedStatement selectFactionWorth) {
        this.identityCache = identityCache;
        this.selectFaction = selectFaction;
        this.selectFactionMaterial = selectFactionMaterial;
        this.selectFactionSpawner = selectFactionSpawner;
        this.selectFactionWorth = selectFactionWorth;
    }

    public static FactionLoader of(Connection connection, IdentityCache identityCache) throws SQLException {
        PreparedStatement selectFaction = connection.prepareStatement(SELECT_FACTION);
        PreparedStatement selectFactionMaterial = connection.prepareStatement(SELECT_FACTION_MATERIAL);
        PreparedStatement selectFactionSpawner = connection.prepareStatement(SELECT_FACTION_SPAWNER);
        PreparedStatement selectFactionWorth = connection.prepareStatement(SELECT_FACTION_WORTH);
        return new FactionLoader(identityCache, selectFaction, selectFactionMaterial, selectFactionSpawner, selectFactionWorth);
    }

    public void load() throws SQLException {
        loadFaction();
        loadFactionMaterial();
        loadFactionSpawner();
        loadFactionWorth();
    }

    public void close() throws SQLException {
        selectFaction.close();
        selectFactionMaterial.close();
        selectFactionSpawner.close();
        selectFactionWorth.close();
    }

    private void loadFaction() throws SQLException {
        ResultSet resultSet = selectFaction.executeQuery();

        while (resultSet.next()) {
            String factionId = resultSet.getString("id");
            identityCache.addFaction(factionId);
        }

        resultSet.close();
    }

    private void loadFactionMaterial() throws SQLException {
        ResultSet resultSet = selectFactionMaterial.executeQuery();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String factionId = resultSet.getString("faction_id");
            int materialId = resultSet.getInt("material_id");
            identityCache.setFactionMaterialId(factionId, materialId, id);
        }

        resultSet.close();
    }

    private void loadFactionSpawner() throws SQLException {
        ResultSet resultSet = selectFactionSpawner.executeQuery();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String factionId = resultSet.getString("faction_id");
            int spawnerId = resultSet.getInt("spawner_id");
            identityCache.setFactionSpawnerId(factionId, spawnerId, id);
        }

        resultSet.close();
    }

    private void loadFactionWorth() throws SQLException {
        ResultSet resultSet = selectFactionWorth.executeQuery();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String factionId = resultSet.getString("faction_id");
            int worthId = resultSet.getInt("worth_id");
            identityCache.setFactionWorthId(factionId, worthId, id);
        }

        resultSet.close();
    }
}
