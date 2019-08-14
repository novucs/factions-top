package net.novucs.ftop.database;

import net.novucs.ftop.entity.IdentityCache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;

public class NameLoader {

    private static final String SELECT_MATERIAL = "SELECT * FROM `material`";
    private static final String SELECT_SPAWNER = "SELECT * FROM `spawner`";
    private static final String SELECT_WORLD = "SELECT * FROM `world`";
    private static final String SELECT_WORTH = "SELECT * FROM `worth`";

    private final IdentityCache identityCache;
    private final PreparedStatement selectMaterial;
    private final PreparedStatement selectSpawner;
    private final PreparedStatement selectWorld;
    private final PreparedStatement selectWorth;

    private NameLoader(IdentityCache identityCache,
                       PreparedStatement selectMaterial,
                       PreparedStatement selectSpawner,
                       PreparedStatement selectWorld,
                       PreparedStatement selectWorth) {
        this.identityCache = identityCache;
        this.selectMaterial = selectMaterial;
        this.selectSpawner = selectSpawner;
        this.selectWorld = selectWorld;
        this.selectWorth = selectWorth;
    }

    public static NameLoader of(Connection connection, IdentityCache identityCache) throws SQLException {
        PreparedStatement selectMaterial = connection.prepareStatement(SELECT_MATERIAL);
        PreparedStatement selectSpawner = connection.prepareStatement(SELECT_SPAWNER);
        PreparedStatement selectWorld = connection.prepareStatement(SELECT_WORLD);
        PreparedStatement selectWorth = connection.prepareStatement(SELECT_WORTH);
        return new NameLoader(identityCache, selectMaterial, selectSpawner, selectWorld, selectWorth);
    }

    public void load() throws SQLException {
        loadMaterial();
        loadSpawner();
        loadWorld();
        loadWorth();
    }

    public void close() throws SQLException {
        selectMaterial.close();
        selectSpawner.close();
        selectWorld.close();
        selectWorth.close();
    }

    private void loadName(PreparedStatement statement, BiConsumer<String, Integer> loader) throws SQLException {
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            loader.accept(name, id);
        }

        resultSet.close();
    }

    private void loadMaterial() throws SQLException {
        loadName(selectMaterial, identityCache::setMaterialId);
    }

    private void loadSpawner() throws SQLException {
        loadName(selectSpawner, identityCache::setSpawnerId);
    }

    private void loadWorld() throws SQLException {
        loadName(selectWorld, identityCache::setWorldId);
    }

    private void loadWorth() throws SQLException {
        loadName(selectWorth, identityCache::setWorthId);
    }
}
