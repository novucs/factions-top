package net.novucs.ftop.database;

import net.novucs.ftop.entity.IdentityCache;

import java.sql.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class WorldModel {

    private static final String INSERT = "INSERT INTO `world` (`name`) VALUES(?)";

    private final List<String> insertionQueue = new LinkedList<>();
    private final IdentityCache identityCache;
    private final PreparedStatement insert;

    private WorldModel(IdentityCache identityCache, PreparedStatement insert) {
        this.identityCache = identityCache;
        this.insert = insert;
    }

    public static WorldModel of(Connection connection, IdentityCache identityCache) throws SQLException {
        PreparedStatement insert = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
        return new WorldModel(identityCache, insert);
    }

    public void executeBatch() throws SQLException {
        insert.executeBatch();

        ResultSet resultSet = insert.getGeneratedKeys();

        for (String world : insertionQueue) {
            if (resultSet.next()) {
                int id = resultSet.getInt(1);
                identityCache.setWorldId(world, id);
            }
        }

        resultSet.close();

        insertionQueue.clear();
    }

    public void close() throws SQLException {
        insert.close();
    }

    public void addBatch(Collection<String> worlds) throws SQLException {
        for (String world : worlds) {
            addBatch(world);
        }
    }

    public void addBatch(String world) throws SQLException {
        if (insertionQueue.contains(world) || identityCache.hasWorld(world)) {
            return;
        }

        insert.setString(1, world);
        insert.addBatch();
        insertionQueue.add(world);
    }
}
