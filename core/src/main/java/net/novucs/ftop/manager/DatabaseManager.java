package net.novucs.ftop.manager;

import com.google.common.collect.Multimap;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.novucs.ftop.database.*;
import net.novucs.ftop.entity.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DatabaseManager {

    private final HikariDataSource dataSource;
    private final IdentityCache identityCache = new IdentityCache();

    public static DatabaseManager create(HikariConfig hikariConfig) throws SQLException {
        // Create the datasource.
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        // Create the database manager.
        DatabaseManager manager = new DatabaseManager(dataSource);

        // Initialize the database.
        Connection connection = dataSource.getConnection();
        manager.init(connection);
        connection.close();

        // Return the new database manager.
        return manager;
    }

    private DatabaseManager(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void init(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `world` (" +
                "`id` INT NOT NULL AUTO_INCREMENT," +
                "`name` VARCHAR(40) NOT NULL UNIQUE," +
                "PRIMARY KEY (`id`))");
        statement.executeUpdate();

        statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `chunk` (" +
                "`id` INT NOT NULL AUTO_INCREMENT," +
                "`world_id` INT NOT NULL," +
                "`x` INT NOT NULL," +
                "`z` INT NOT NULL," +
                "PRIMARY KEY (`id`)," +
                "FOREIGN KEY (`world_id`) REFERENCES world(`id`)," +
                "UNIQUE (`world_id`, `x`, `z`))");
        statement.executeUpdate();

        statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `worth` (" +
                "`id` INT NOT NULL AUTO_INCREMENT," +
                "`name` VARCHAR (40) NOT NULL UNIQUE," +
                "PRIMARY KEY (`id`))");
        statement.executeUpdate();

        statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `chunk_worth` (" +
                "`id` INT NOT NULL AUTO_INCREMENT," +
                "`chunk_id` INT NOT NULL," +
                "`worth_id` INT NOT NULL," +
                "`worth` FLOAT NOT NULL," +
                "PRIMARY KEY (`id`)," +
                "FOREIGN KEY (`chunk_id`) REFERENCES chunk(`id`)," +
                "FOREIGN KEY (`worth_id`) REFERENCES worth(`id`)," +
                "UNIQUE(`chunk_id`, `worth_id`))");
        statement.executeUpdate();

        statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `material` (" +
                "`id` INT NOT NULL AUTO_INCREMENT," +
                "`name` VARCHAR(40) NOT NULL UNIQUE," +
                "PRIMARY KEY (`id`))");
        statement.executeUpdate();

        statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `chunk_material_count` (" +
                "`id` INT NOT NULL AUTO_INCREMENT," +
                "`chunk_id` INT NOT NULL," +
                "`material_id` INT NOT NULL," +
                "`count` INT NOT NULL," +
                "PRIMARY KEY (`id`)," +
                "FOREIGN KEY (`chunk_id`) REFERENCES chunk(`id`)," +
                "FOREIGN KEY (`material_id`) REFERENCES material(`id`)," +
                "UNIQUE (`chunk_id`, `material_id`))");
        statement.executeUpdate();

        statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `spawner` (" +
                "`id` INT NOT NULL AUTO_INCREMENT," +
                "`name` VARCHAR(40) NOT NULL UNIQUE," +
                "PRIMARY KEY (`id`))");
        statement.executeUpdate();

        statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `chunk_spawner_count` (" +
                "`id` INT NOT NULL AUTO_INCREMENT," +
                "`chunk_id` INT NOT NULL," +
                "`spawner_id` INT NOT NULL," +
                "`count` INT NOT NULL," +
                "PRIMARY KEY (`id`)," +
                "FOREIGN KEY (`chunk_id`) REFERENCES chunk(`id`)," +
                "FOREIGN KEY (`spawner_id`) REFERENCES spawner(`id`)," +
                "UNIQUE (`chunk_id`, `spawner_id`))");
        statement.executeUpdate();

        statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `block` (" +
                "`id` INT NOT NULL AUTO_INCREMENT, " +
                "`world_id` INT NOT NULL, " +
                "`x` INT NOT NULL, " +
                "`y` INT NOT NULL, " +
                "`z` INT NOT NULL, " +
                "PRIMARY KEY (`id`), " +
                "FOREIGN KEY (`world_id`) REFERENCES world(`id`), " +
                "UNIQUE (`world_id`, `x`, `y`, `z`))");
        statement.executeUpdate();

        statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `sign` (" +
                "`id` INT NOT NULL AUTO_INCREMENT, " +
                "`block_id` INT NOT NULL UNIQUE, " +
                "`rank` INT NOT NULL, " +
                "PRIMARY KEY (`id`), " +
                "FOREIGN KEY (`block_id`) REFERENCES block(`id`))");
        statement.executeUpdate();

        statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `faction` (" +
                "`id` VARCHAR(40) NOT NULL, " +
                "`name` VARCHAR(40) NOT NULL UNIQUE, " +
                "`total_worth` FLOAT NOT NULL, " +
                "`total_spawners` INT NOT NULL, " +
                "PRIMARY KEY (`id`))");
        statement.executeUpdate();

        statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `faction_worth` (" +
                "`id` INT NOT NULL AUTO_INCREMENT, " +
                "`faction_id` VARCHAR(40) NOT NULL, " +
                "`worth_id` INT NOT NULL, " +
                "`worth` FLOAT NOT NULL, " +
                "PRIMARY KEY (`id`), " +
                "FOREIGN KEY (`faction_id`) REFERENCES faction(`id`), " +
                "FOREIGN KEY (`worth_id`) REFERENCES worth(`id`), " +
                "UNIQUE (`faction_id`, `worth_id`))");
        statement.executeUpdate();

        statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `faction_material_count` (" +
                "`id` INT NOT NULL AUTO_INCREMENT, " +
                "`faction_id` VARCHAR(40) NOT NULL, " +
                "`material_id` INT NOT NULL, " +
                "`count` INT NOT NULL, " +
                "PRIMARY KEY (`id`), " +
                "FOREIGN KEY (`faction_id`) REFERENCES faction(`id`), " +
                "FOREIGN KEY (`material_id`) REFERENCES material(`id`), " +
                "UNIQUE (`faction_id`, `material_id`))");
        statement.executeUpdate();

        statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `faction_spawner_count` (" +
                "`id` INT NOT NULL AUTO_INCREMENT, " +
                "`faction_id` VARCHAR(40) NOT NULL, " +
                "`spawner_id` INT NOT NULL, " +
                "`count` INT NOT NULL, " +
                "PRIMARY KEY (`id`), " +
                "FOREIGN KEY (`faction_id`) REFERENCES faction(`id`), " +
                "FOREIGN KEY (`spawner_id`) REFERENCES spawner(`id`), " +
                "UNIQUE (`faction_id`, `spawner_id`))");
        statement.executeUpdate();
    }

    public IdentityCache getIdentityCache() {
        return identityCache;
    }

    public DataDump load() throws SQLException {
        Connection connection = dataSource.getConnection();

        NameLoader nameLoader = NameLoader.of(connection, identityCache);
        nameLoader.load();
        nameLoader.close();

        FactionLoader factionLoader = FactionLoader.of(connection, identityCache);
        factionLoader.load();
        factionLoader.close();

        ChunkLoader chunkLoader = ChunkLoader.of(connection, identityCache);
        Map<ChunkPos, ChunkWorth> chunks = chunkLoader.load();
        chunkLoader.close();

        SignLoader signLoader = SignLoader.of(connection, identityCache);
        Multimap<Integer, BlockPos> signs = signLoader.load();
        signLoader.close();

        connection.close();

        return new DataDump(chunks, signs);
    }

    public void save(List<Map.Entry<ChunkPos, ChunkWorth>> chunks,
                     List<FactionWorth> factions,
                     Set<String> deletedFactions,
                     Collection<Map.Entry<BlockPos, Integer>> createdSigns,
                     Collection<BlockPos> deletedSigns) throws SQLException {
        Connection connection = dataSource.getConnection();

        ChunkModel chunkModel = new ChunkModel(connection, identityCache);
        chunkModel.persist(chunks);

        FactionModel factionModel = new FactionModel(connection, identityCache);
        factionModel.persist(factions, deletedFactions);

        BlockModel blockModel = BlockModel.of(connection, identityCache);

        for (Map.Entry<BlockPos, Integer> entry : createdSigns) {
            blockModel.addBatch(entry.getKey());
        }

        blockModel.executeBatch();
        blockModel.close();

        SignModel signModel = SignModel.of(connection, identityCache);
        signModel.addBatch(createdSigns);
        signModel.addBatchDelete(deletedSigns);
        signModel.executeBatch();
        signModel.close();

        connection.close();
    }

    public void close() {
        dataSource.close();
    }

    public class DataDump {

        private final Map<ChunkPos, ChunkWorth> chunks;
        private final Multimap<Integer, BlockPos> signs;

        private DataDump(Map<ChunkPos, ChunkWorth> chunks, Multimap<Integer, BlockPos> signs) {
            this.chunks = chunks;
            this.signs = signs;
        }

        public Map<ChunkPos, ChunkWorth> getChunks() {
            return chunks;
        }

        public Multimap<Integer, BlockPos> getSigns() {
            return signs;
        }
    }
}
