package net.novucs.ftop.database;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.novucs.ftop.entity.BlockPos;
import net.novucs.ftop.entity.IdentityCache;
import net.novucs.ftop.manager.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SignLoader {

    private static final String SELECT_BLOCK = "SELECT * FROM `" + DatabaseManager.prefix + "block`";
    private static final String SELECT_SIGN = "SELECT * FROM `" + DatabaseManager.prefix + "sign`";

    private final IdentityCache identityCache;
    private final PreparedStatement selectBlock;
    private final PreparedStatement selectSign;

    public SignLoader(IdentityCache identityCache, PreparedStatement selectBlock, PreparedStatement selectSign) {
        this.identityCache = identityCache;
        this.selectBlock = selectBlock;
        this.selectSign = selectSign;
    }

    public static SignLoader of(Connection connection, IdentityCache identityCache) throws SQLException {
        PreparedStatement selectBlock = connection.prepareStatement(SELECT_BLOCK);
        PreparedStatement selectSign = connection.prepareStatement(SELECT_SIGN);
        return new SignLoader(identityCache, selectBlock, selectSign);
    }

    public Multimap<Integer, BlockPos> load() throws SQLException {
        loadBlock();

        return loadSign();
    }

    public void close() throws SQLException {
        selectBlock.close();
        selectSign.close();
    }

    private void loadBlock() throws SQLException {
        ResultSet resultSet = selectBlock.executeQuery();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            int worldId = resultSet.getInt("world_id");
            int x = resultSet.getInt("x");
            int y = resultSet.getInt("y");
            int z = resultSet.getInt("z");
            identityCache.setBlockId(worldId, x, y, z, id);
        }

        resultSet.close();
    }

    private Multimap<Integer, BlockPos> loadSign() throws SQLException {
        ResultSet resultSet = selectSign.executeQuery();
        Multimap<Integer, BlockPos> signs = HashMultimap.create();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            int blockId = resultSet.getInt("block_id");
            int rank = resultSet.getInt("rank");

            identityCache.setSignId(blockId, id);
            identityCache.getBlock(blockId).ifPresent(block ->
                    signs.put(rank, block));
        }

        resultSet.close();
        return signs;
    }
}
