package net.novucs.ftop;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DatabaseManager {

    private final HikariDataSource dataSource;

    public static DatabaseManager create(HikariConfig hikariConfig) throws SQLException {
        // Create the datasource.
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        // Test the connection.
        Connection connection = dataSource.getConnection();
        connection.close();

        // Return the new database manager.
        return new DatabaseManager(dataSource);
    }

    private DatabaseManager(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void init(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `world` (" +
                "`id` INT NOT NULL AUTO_INCREMENT," +
                "`name` VARCHAR(40) NOT NULL," +
                "PRIMARY KEY (`id`))");
        statement.executeUpdate();

        statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `chunk` (" +
                "`id` INT NOT NULL AUTO_INCREMENT," +
                "`world_id` INT NOT NULL," +
                "`x` INT NOT NULL," +
                "`z` INT NOT NULL," +
                "PRIMARY KEY (`id`)," +
                "FOREIGN KEY (`world_id`) REFERENCES world(`id`))");
        statement.executeUpdate();

        statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `worth` (" +
                "`id` INT NOT NULL AUTO_INCREMENT," +
                "`name` VARCHAR (40) NOT NULL," +
                "PRIMARY KEY (`id`))");
        statement.executeUpdate();

        statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `chunk_worth` (" +
                "`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "`chunk_id` INT NOT NULL," +
                "`worth_id` INT NOT NULL," +
                "`worth` FLOAT NOT NULL," +
                "PRIMARY KEY (`id`)," +
                "FOREIGN KEY (`chunk_id`) REFERENCES chunk(`id`)," +
                "FOREIGN KEY (`worth_id`) REFERENCES worth(`id`))");
        statement.executeUpdate();

        statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `material` (" +
                "`id` INT NOT NULL AUTO_INCREMENT," +
                "`name` VARCHAR(40) NOT NULL," +
                "PRIMARY KEY (`id`))");
        statement.executeUpdate();

        statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `chunk_material_count` (" +
                "`id` INT NOT NULL AUTO_INCREMENT," +
                "`chunk_id` INT NOT NULL," +
                "`material_id` INT NOT NULL," +
                "`count` INT NOT NULL," +
                "PRIMARY KEY (`id`)," +
                "FOREIGN KEY (`chunk_id`) REFERENCES chunk(`id`)," +
                "FOREIGN KEY (`material_id`) REFERENCES material(`id`))");
        statement.executeUpdate();

        statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `spawner` (" +
                "`id` INT NOT NULL AUTO_INCREMENT," +
                "`name` VARCHAR(40) NOT NULL," +
                "PRIMARY KEY (`id`))");
        statement.executeUpdate();

        statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `chunk_spawner_count` (" +
                "`id` INT NOT NULL AUTO_INCREMENT," +
                "`chunk_id` INT NOT NULL," +
                "`spawner_id` INT NOT NULL," +
                "`count` INT NOT NULL," +
                "PRIMARY KEY (`id`)," +
                "FOREIGN KEY (`chunk_id`) REFERENCES chunk(`id`)," +
                "FOREIGN KEY (`spawner_id`) REFERENCES spawner(`id`))");
        statement.executeUpdate();
    }

    public Map<ChunkPos, ChunkWorth> load() throws SQLException {
        Map<ChunkPos, ChunkWorth> target = new HashMap<>();

        Map<WorthType, Double> worth;
        Map<Material, Integer> materialCount;
        Map<EntityType, Integer> spawnerCount;

        Connection connection = dataSource.getConnection();
        init(connection);

        Map<Integer, ChunkPos> chunks = getChunkMap(connection);

        for (Map.Entry<Integer, ChunkPos> entry : chunks.entrySet()) {
            worth = getChunkWorth(connection, entry.getKey());
            materialCount = getChunkMaterialCount(connection, entry.getKey());
            spawnerCount = getChunkSpawnerCount(connection, entry.getKey());
            target.put(entry.getValue(), new ChunkWorth(worth, materialCount, spawnerCount));
        }

        return target;
    }

    private Map<WorthType, Double> getChunkWorth(Connection connection, int chunkId) throws SQLException {
        Map<WorthType, Double> target = new EnumMap<>(WorthType.class);

        Map<Integer, WorthType> worthMap = getWorthMap(connection);

        PreparedStatement statement = connection.prepareStatement("SELECT `worth_id`,`worth` FROM `chunk_worth` WHERE `chunk_id`=?");
        statement.setInt(1, chunkId);
        ResultSet set = statement.executeQuery();

        while (set.next()) {
            WorthType worthType = worthMap.get(set.getInt("worth_id"));
            double worth = set.getDouble("worth");
            target.put(worthType, worth);
        }

        set.close();
        statement.close();

        return target;
    }

    private <T extends Enum<T>> Map<T, Integer> getCount(Connection connection, Class<T> clazz, String countType, int chunkId) throws SQLException {
        Map<T, Integer> target = new EnumMap<>(clazz);
        Map<Integer, T> supportMap = getEnumMap(connection, clazz, countType);

        PreparedStatement statement = connection.prepareStatement("SELECT ?,`count` FROM ? WHERE `chunk_id`=?");
        statement.setString(1, countType + "_id");
        statement.setString(2, "chunk_" + countType + "_count");
        statement.setInt(3, chunkId);
        ResultSet set = statement.executeQuery();

        while (set.next()) {
            T countTypeEnum = supportMap.get(set.getInt(countType + "_id"));
            int count = set.getInt("count");
            target.put(countTypeEnum, count);
        }

        set.close();
        statement.close();

        return target;
    }

    private Map<Material, Integer> getChunkMaterialCount(Connection connection, int chunkId) throws SQLException {
        return getCount(connection, Material.class, "material", chunkId);
    }

    private Map<EntityType, Integer> getChunkSpawnerCount(Connection connection, int chunkId) throws SQLException {
        return getCount(connection, EntityType.class, "spawner", chunkId);
    }

    private <T extends Enum<T>> Map<Integer, T> getEnumMap(Connection connection, Class<T> clazz, String table) throws SQLException {
        Map<Integer, T> target = new HashMap<>();

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM ?");
        statement.setString(1, table);
        ResultSet set = statement.executeQuery();

        while (set.next()) {
            int id = set.getInt("id");
            Optional<T> parsed = StringUtils.parseEnum(clazz, set.getString("name"));
            if (parsed.isPresent()) {
                target.put(id, parsed.get());
            }
        }

        set.close();
        statement.close();

        return target;
    }

    private Map<Integer, WorthType> getWorthMap(Connection connection) throws SQLException {
        return getEnumMap(connection, WorthType.class, "worth");
    }

    private Map<Integer, Material> getMaterialMap(Connection connection) throws SQLException {
        return getEnumMap(connection, Material.class, "material");
    }

    private Map<Integer, EntityType> getSpawnerMap(Connection connection) throws SQLException {
        return getEnumMap(connection, EntityType.class, "spawner");
    }

    private Map<Integer, ChunkPos> getChunkMap(Connection connection) throws SQLException {
        Map<Integer, ChunkPos> target = new HashMap<>();

        Map<Integer, String> worldMap = getWorldMap(connection);

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `chunk`");
        ResultSet set = statement.executeQuery();

        while (set.next()) {
            int id = set.getInt("id");
            String world = worldMap.get(set.getInt("world_id"));
            int x = set.getInt("x");
            int z = set.getInt("z");
            target.put(id, ChunkPos.of(world, x, z));
        }

        set.close();
        statement.close();

        return target;
    }

    private Map<Integer, String> getWorldMap(Connection connection) throws SQLException {
        Map<Integer, String> target = new HashMap<>();

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM `world`");
        ResultSet set = statement.executeQuery();

        while (set.next()) {
            int id = set.getInt("id");
            String name = set.getString("name");
            target.put(id, name);
        }

        set.close();
        statement.close();

        return target;
    }
}
