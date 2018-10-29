package net.novucs.ftop.database;

import net.novucs.ftop.entity.IdentityCache;
import net.novucs.ftop.manager.DatabaseManager;
import org.bukkit.Material;

import java.sql.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MaterialModel {

    private static final String INSERT = "INSERT INTO `" + DatabaseManager.prefix + "material` (`name`) VALUES(?)";

    private final List<Material> insertionQueue = new LinkedList<>();
    private final IdentityCache identityCache;
    private final PreparedStatement insert;

    private MaterialModel(IdentityCache identityCache, PreparedStatement insert) {
        this.identityCache = identityCache;
        this.insert = insert;
    }

    public static MaterialModel of(Connection connection, IdentityCache identityCache) throws SQLException {
        PreparedStatement insert = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
        return new MaterialModel(identityCache, insert);
    }

    public void executeBatch() throws SQLException {
        insert.executeBatch();

        ResultSet resultSet = insert.getGeneratedKeys();

        for (Material material : insertionQueue) {
            if (resultSet.next()) {
                int id = resultSet.getInt(1);
                identityCache.setMaterialId(material.name(), id);
            }
        }

        resultSet.close();

        insertionQueue.clear();
    }

    public void close() throws SQLException {
        insert.close();
    }

    public void addBatch(Collection<Material> materials) throws SQLException {
        for (Material material : materials) {
            addBatch(material);
        }
    }

    public void addBatch(Material material) throws SQLException {
        if (insertionQueue.contains(material) || identityCache.hasMaterial(material.name())) {
            return;
        }

        insert.setString(1, material.name());
        insert.addBatch();
        insertionQueue.add(material);
    }
}
