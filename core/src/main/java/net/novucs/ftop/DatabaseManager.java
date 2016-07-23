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
                "`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
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

        PreparedStatement statement = connection.prepareStatement("SELECT `" + countType + "_id`,`count` " +
                "FROM `chunk_" + countType + "_count` WHERE `chunk_id`=?");
        statement.setInt(1, chunkId);
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

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + table);
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

    public void save(Map<ChunkPos, ChunkWorth> chunkWorthMap) throws SQLException {
        Connection connection = dataSource.getConnection();
        init(connection);

        for (Map.Entry<ChunkPos, ChunkWorth> entry : chunkWorthMap.entrySet()) {
            int chunkId = saveChunk(connection, entry.getKey());
            saveChunkWorth(connection, chunkId, entry.getValue());
        }
    }

    private void saveChunkWorth(Connection connection, int chunkId, ChunkWorth chunkWorth) throws SQLException {
        for (Map.Entry<WorthType, Double> entry : chunkWorth.getWorth().entrySet()) {
            saveChunkWorth(connection, chunkId, entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Material, Integer> entry : chunkWorth.getMaterials().entrySet()) {
            saveChunkMaterial(connection, chunkId, entry.getKey(), entry.getValue());
        }

        for (Map.Entry<EntityType, Integer> entry : chunkWorth.getSpawners().entrySet()) {
            saveChunkSpawner(connection, chunkId, entry.getKey(), entry.getValue());
        }
    }

    private int saveChunkMaterial(Connection connection, int chunkId, Material material, int count) throws SQLException {
        int materialId = saveMaterial(connection, material);
        int id = getChunkMaterialId(connection, chunkId, materialId);
        if (id > 0) {
            PreparedStatement statement = connection.prepareStatement("UPDATE `chunk_material_count` SET `count` = ? WHERE `id` = ?");
            statement.setInt(1, count);
            statement.setInt(2, id);
            statement.executeUpdate();
            return id;
        }

        PreparedStatement statement = connection.prepareStatement("INSERT INTO `chunk_material_count` (`chunk_id`, `material_id`, `count`) VALUES(?, ?, ?)");
        statement.setInt(1, chunkId);
        statement.setInt(2, materialId);
        statement.setInt(3, count);

        statement.executeUpdate();
        ResultSet set = statement.getGeneratedKeys();

        set.next();
        return set.getInt(1);
    }

    private int getChunkMaterialId(Connection connection, int chunkId, int materialId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT `id` FROM `chunk_material_count` WHERE `chunk_id` = ? AND `material_id` = ?");
        statement.setInt(1, chunkId);
        statement.setInt(2, materialId);
        ResultSet set = statement.executeQuery();
        if (set.next()) {
            return set.getInt("id");
        }

        return -1;
    }

    private int saveChunkSpawner(Connection connection, int chunkId, EntityType spawner, int count) throws SQLException {
        int spawnerId = saveSpawner(connection, spawner);
        int id = getChunkSpawnerId(connection, chunkId, spawnerId);
        if (id > 0) {
            PreparedStatement statement = connection.prepareStatement("UPDATE `chunk_spawner_count` SET `count` = ? WHERE `id` = ?");
            statement.setInt(1, count);
            statement.setInt(2, id);
            statement.executeUpdate();
            return id;
        }

        PreparedStatement statement = connection.prepareStatement("INSERT INTO `chunk_spawner_count` (`chunk_id`, `spawner_id`, `count`) VALUES(?, ?, ?)");
        statement.setInt(1, chunkId);
        statement.setInt(2, spawnerId);
        statement.setInt(3, count);

        statement.executeUpdate();
        ResultSet set = statement.getGeneratedKeys();

        set.next();
        return set.getInt(1);
    }

    private int getChunkSpawnerId(Connection connection, int chunkId, int spawnerId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT `id` FROM `chunk_spawner_count` WHERE `chunk_id` = ? AND `spawner_id` = ?");
        statement.setInt(1, chunkId);
        statement.setInt(2, spawnerId);
        ResultSet set = statement.executeQuery();
        if (set.next()) {
            return set.getInt("id");
        }

        return -1;
    }

    private int saveChunkWorth(Connection connection, int chunkId, WorthType worthType, double worth) throws SQLException {
        int worthId = saveWorth(connection, worthType);
        int id = getChunkWorthId(connection, chunkId, worthId);
        if (id > 0) {
            PreparedStatement statement = connection.prepareStatement("UPDATE `chunk_worth` SET `worth` = ? WHERE `id` = ?");
            statement.setDouble(1, worth);
            statement.setInt(2, id);
            statement.executeUpdate();
            return id;
        }

        PreparedStatement statement = connection.prepareStatement("INSERT INTO `chunk_worth` (`chunk_id`, `worth_id`, `worth`) VALUES(?, ?, ?)");
        statement.setInt(1, chunkId);
        statement.setInt(2, worthId);
        statement.setDouble(3, worth);

        statement.executeUpdate();
        ResultSet set = statement.getGeneratedKeys();

        set.next();
        return set.getInt(1);
    }

    private int getChunkWorthId(Connection connection, int chunkId, int worthId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT `id` FROM `chunk_worth` WHERE `chunk_id` = ? AND `worth_id` = ?");
        statement.setInt(1, chunkId);
        statement.setInt(2, worthId);
        ResultSet set = statement.executeQuery();
        if (set.next()) {
            return set.getInt("id");
        }

        return -1;
    }

    private int saveChunk(Connection connection, ChunkPos pos) throws SQLException {
        int worldId = saveWorld(connection, pos.getWorld());
        int id = getChunkId(connection, worldId, pos.getX(), pos.getZ());
        if (id > 0) {
            return id;
        }

        PreparedStatement statement = connection.prepareStatement("INSERT INTO `chunk` (`world_id`, `x`, `z`) VALUES(?, ?, ?)");
        statement.setInt(1, worldId);
        statement.setInt(2, pos.getX());
        statement.setInt(3, pos.getZ());

        statement.executeUpdate();
        ResultSet set = statement.getGeneratedKeys();

        set.next();
        return set.getInt(1);
    }

    private int getChunkId(Connection connection, int worldId, int x, int z) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT `id` FROM `chunk` WHERE `world_id` = ? AND `x` = ? AND `z` = ?");
        statement.setInt(1, worldId);
        statement.setInt(2, x);
        statement.setInt(3, z);
        ResultSet set = statement.executeQuery();
        if (set.next()) {
            return set.getInt("id");
        }

        return -1;
    }

    private int saveName(Connection connection, String table, String name) throws SQLException {
        int id = getNameId(connection, table, name);
        if (id > 0) {
            return id;
        }

        PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + table + "` (`name`) VALUES(?)");
        statement.setString(1, name);
        statement.executeUpdate();

        ResultSet set = statement.getGeneratedKeys();
        set.next();
        return set.getInt(1);
    }

    private int getNameId(Connection connection, String table, String name) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT `id` FROM `" + table + "` WHERE `name` = ?");
        statement.setString(1, name);
        ResultSet set = statement.executeQuery();
        if (set.next()) {
            return set.getInt("id");
        }

        return -1;
    }

    private int saveMaterial(Connection connection, Material material) throws SQLException {
        return saveName(connection, "material", material.name());
    }

    private int saveSpawner(Connection connection, EntityType spawner) throws SQLException {
        return saveName(connection, "spawner", spawner.name());
    }

    private int saveWorld(Connection connection, String world) throws SQLException {
        return saveName(connection, "world", world);
    }

    private int saveWorth(Connection connection, WorthType worthType) throws SQLException {
        return saveName(connection, "worth", worthType.name());
    }
}
