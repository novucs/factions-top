package net.novucs.ftop.database;

import net.novucs.ftop.entity.IdentityCache;
import org.bukkit.entity.EntityType;

import java.sql.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SpawnerModel {

    private static final String INSERT = "INSERT INTO `spawner` (`name`) VALUES(?)";

    private final List<EntityType> insertionQueue = new LinkedList<>();
    private final IdentityCache identityCache;
    private final PreparedStatement insert;

    private SpawnerModel(IdentityCache identityCache, PreparedStatement insert) {
        this.identityCache = identityCache;
        this.insert = insert;
    }

    public static SpawnerModel of(Connection connection, IdentityCache identityCache) throws SQLException {
        PreparedStatement insert = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
        return new SpawnerModel(identityCache, insert);
    }

    public void executeBatch() throws SQLException {
        insert.executeBatch();

        ResultSet resultSet = insert.getGeneratedKeys();

        for (EntityType spawner : insertionQueue) {
            if (resultSet.next()) {
                int id = resultSet.getInt(1);
                identityCache.setSpawnerId(spawner.name(), id);
            }
        }

        resultSet.close();

        insertionQueue.clear();
    }

    public void close() throws SQLException {
        insert.close();
    }

    public void addBatch(Collection<EntityType> spawners) throws SQLException {
        for (EntityType spawner : spawners) {
            addBatch(spawner);
        }
    }

    public void addBatch(EntityType spawner) throws SQLException {
        if (insertionQueue.contains(spawner) || identityCache.hasSpawner(spawner.name())) {
            return;
        }

        insert.setString(1, spawner.name());
        insert.addBatch();
        insertionQueue.add(spawner);
    }
}
