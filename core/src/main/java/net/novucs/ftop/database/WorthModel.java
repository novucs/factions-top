package net.novucs.ftop.database;

import net.novucs.ftop.WorthType;
import net.novucs.ftop.entity.IdentityCache;
import net.novucs.ftop.manager.DatabaseManager;

import java.sql.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class WorthModel {

    private static final String INSERT = "INSERT INTO `" + DatabaseManager.prefix + "worth` (`name`) VALUES(?)";

    private final List<WorthType> insertionQueue = new LinkedList<>();
    private final IdentityCache identityCache;
    private final PreparedStatement insert;

    private WorthModel(IdentityCache identityCache, PreparedStatement insert) {
        this.identityCache = identityCache;
        this.insert = insert;
    }

    public static WorthModel of(Connection connection, IdentityCache identityCache) throws SQLException {
        PreparedStatement insert = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
        return new WorthModel(identityCache, insert);
    }

    public void executeBatch() throws SQLException {
        insert.executeBatch();

        ResultSet resultSet = insert.getGeneratedKeys();

        for (WorthType worthType : insertionQueue) {
            if (resultSet.next()) {
                int id = resultSet.getInt(1);
                identityCache.setWorthId(worthType.name(), id);
            }
        }

        resultSet.close();

        insertionQueue.clear();
    }

    public void close() throws SQLException {
        insert.close();
    }

    public void addBatch(Collection<WorthType> worthTypes) throws SQLException {
        for (WorthType worthType : worthTypes) {
            addBatch(worthType);
        }
    }

    public void addBatch(WorthType worthType) throws SQLException {
        if (insertionQueue.contains(worthType) || identityCache.hasWorth(worthType.name())) {
            return;
        }

        insert.setString(1, worthType.name());
        insert.addBatch();
        insertionQueue.add(worthType);
    }
}
