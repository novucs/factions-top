package net.novucs.ftop;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zaxxer.hikari.HikariConfig;
import net.novucs.ftop.hook.VaultEconomyHook;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Settings {

    private static final int LATEST_VERSION = 1;
    private static final String HEADER = "FactionsTop configuration.\n";

    private final FactionsTopPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    // General settings.
    private List<String> commandAliases;
    private List<String> ignoredFactionIds;
    private int factionsPerPage;
    private int liquidUpdateTicks;
    private int chunkQueueSize;
    private long chunkRecalculateMillis;
    private HikariConfig hikariConfig;
    private Map<WorthType, Boolean> enabled;
    private Map<WorthType, Boolean> detailed;
    private Map<RecalculateReason, Boolean> performRecalculate;
    private Map<RecalculateReason, Boolean> bypassRecalculateDelay;
    private Map<EntityType, Double> spawnerPrices;
    private Map<Material, Double> blockPrices;

    public Settings(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    public List<String> getCommandAliases() {
        return commandAliases;
    }

    public List<String> getIgnoredFactionIds() {
        return ignoredFactionIds;
    }

    public int getFactionsPerPage() {
        return factionsPerPage;
    }

    public int getLiquidUpdateTicks() {
        return liquidUpdateTicks;
    }

    public int getChunkQueueSize() {
        return chunkQueueSize;
    }

    public long getChunkRecalculateMillis() {
        return chunkRecalculateMillis;
    }

    public HikariConfig getHikariConfig() {
        return hikariConfig;
    }

    public boolean isEnabled(WorthType worthType) {
        return enabled.getOrDefault(worthType, false);
    }

    public boolean isDetailed(WorthType worthType) {
        return detailed.getOrDefault(worthType, false);
    }

    public boolean isPerformRecalculate(RecalculateReason reason) {
        return performRecalculate.getOrDefault(reason, false);
    }

    public boolean isBypassRecalculateDelay(RecalculateReason reason) {
        return bypassRecalculateDelay.getOrDefault(reason, false);
    }

    public double getSpawnerPrice(EntityType entityType) {
        return spawnerPrices.getOrDefault(entityType, 0d);
    }

    public double getBlockPrice(Material material) {
        return blockPrices.getOrDefault(material, 0d);
    }

    private void set(String path, Object val) {
        config.set(path, val);
    }

    private boolean getBoolean(String path, boolean def) {
        config.addDefault(path, def);
        return config.getBoolean(path);
    }

    private int getInt(String path, int def) {
        config.addDefault(path, def);
        return config.getInt(path);
    }

    private long getLong(String path, long def) {
        config.addDefault(path, def);
        return config.getLong(path);
    }

    private double getDouble(String path, double def) {
        config.addDefault(path, def);
        return config.getDouble(path);
    }

    private String getString(String path, String def) {
        config.addDefault(path, def);
        return config.getString(path);
    }

    private ConfigurationSection getOrCreateSection(String key) {
        return config.getConfigurationSection(key) == null ?
                config.createSection(key) : config.getConfigurationSection(key);
    }

    private ConfigurationSection getOrDefaultSection(String key) {
        return config.getConfigurationSection(key).getKeys(false).isEmpty() ?
                config.getDefaults().getConfigurationSection(key) : config.getConfigurationSection(key);
    }

    private <T> List<?> getList(String key, List<T> def) {
        config.addDefault(key, def);
        return config.getList(key, config.getList(key));
    }

    private <E> List<E> getList(String key, List<E> def, Class<E> type) {
        try {
            return castList(type, getList(key, def));
        } catch (ClassCastException e) {
            return def;
        }
    }

    private <E> List<E> castList(Class<? extends E> type, List<?> toCast) throws ClassCastException {
        return toCast.stream().map(type::cast).collect(Collectors.toList());
    }

    private <T extends Enum<T>> EnumMap<T, Boolean> parseStateMap(Class<T> type, String key, boolean def) {
        EnumMap<T, Boolean> target = new EnumMap<>(type);
        ConfigurationSection section = getOrDefaultSection(key);
        for (String name : section.getKeys(false)) {
            // Warn user if unable to parse enum.
            Optional<T> parsed = StringUtils.parseEnum(type, name);
            if (!parsed.isPresent()) {
                plugin.getLogger().warning("Invalid " + type.getSimpleName() + ": " + name);
                continue;
            }

            // Add the parsed enum and value to the target map.
            target.put(parsed.get(), section.getBoolean(name, def));
        }
        return target;
    }

    private <T extends Enum<T>> void addDefaults(Class<T> type, String key, boolean def, List<T> exempt) {
        ConfigurationSection section = getOrCreateSection(key);
        for (T target : type.getEnumConstants()) {
            section.addDefault(target.name(), exempt.contains(target) != def);
        }
    }

    private <T extends Enum<T>> EnumMap<T, Double> parsePriceMap(Class<T> type, String key, double def) {
        EnumMap<T, Double> target = new EnumMap<>(type);
        ConfigurationSection section = getOrDefaultSection(key);
        for (String name : section.getKeys(false)) {
            // Warn user if unable to parse enum.
            Optional<T> parsed = StringUtils.parseEnum(type, name);
            if (!parsed.isPresent()) {
                plugin.getLogger().warning("Invalid " + type.getSimpleName() + ": " + name);
                continue;
            }

            // Add the parsed enum and value to the target map.
            target.put(parsed.get(), section.getDouble(name, def));
        }
        return target;
    }

    private <T extends Enum<T>> void addDefaults(String key, Map<T, Double> prices) {
        ConfigurationSection section = getOrCreateSection(key);
        prices.forEach((type, price) -> section.addDefault(type.name(), price));
    }

    private <T extends Enum<T>> EnumMap<T, Double> parseDefPrices(Class<T> type, Map<String, Double> def) {
        EnumMap<T, Double> target = new EnumMap<>(type);
        def.forEach((name, price) -> {
            Optional<T> parsed = StringUtils.parseEnum(type, name);
            if (parsed.isPresent()) {
                target.put(parsed.get(), price);
            }
        });
        return target;
    }

    private HikariConfig loadHikariConfig() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(getString("settings.database.jdbc-url", "jdbc:h2:./plugins/FactionsTop/database"));
        hikariConfig.setUsername(getString("settings.database.username", "root"));
        hikariConfig.setPassword(getString("settings.database.password", "pa$$w0rd"));
        hikariConfig.setMaximumPoolSize(getInt("settings.database.maximum-pool-size", 10));
        hikariConfig.setMaxLifetime(getLong("settings.database.max-lifetime", 5000));
        hikariConfig.setIdleTimeout(getLong("settings.database.idle-timeout", 5000));
        hikariConfig.setConnectionTimeout(getLong("settings.database.connection-timeout", 5000));
        hikariConfig.setThreadFactory(new ThreadFactoryBuilder().setDaemon(true)
                .setNameFormat("factions-top-sql-pool-%d").build());
        return hikariConfig;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void load() throws IOException, InvalidConfigurationException {
        // Create then load the configuration and file.
        configFile = new File(plugin.getDataFolder() + File.separator + "config.yml");
        configFile.getParentFile().mkdirs();
        configFile.createNewFile();

        config = new YamlConfiguration();
        config.load(configFile);

        // Load all configuration values into memory.
        int version = getInt("config-version", 0);
        commandAliases = getList("settings.command-aliases", Collections.singletonList("f top"), String.class);
        ignoredFactionIds = getList("settings.ignored-faction-ids", Arrays.asList("none", "safezone", "warzone", "0", "-1", "-2"), String.class);
        factionsPerPage = getInt("settings.factions-per-page", 9);
        liquidUpdateTicks = getInt("settings.liquid-update-ticks", 100);
        if (plugin.getEconomyHook() instanceof VaultEconomyHook) {
            ((VaultEconomyHook) plugin.getEconomyHook()).setLiquidUpdateTicks(liquidUpdateTicks);
        }
        chunkQueueSize = getInt("settings.chunk-queue-size", 200);
        chunkRecalculateMillis = getLong("settings.chunk-recalculate-millis", 120000);

        hikariConfig = loadHikariConfig();

        addDefaults(WorthType.class, "settings.enabled", true, Collections.emptyList());
        enabled = parseStateMap(WorthType.class, "settings.enabled", false);
        plugin.getEconomyHook().setFactionEnabled(isEnabled(WorthType.FACTION_BALANCE));
        plugin.getEconomyHook().setPlayerEnabled(isEnabled(WorthType.PLAYER_BALANCE));

        addDefaults(WorthType.class, "settings.detailed", true, Collections.emptyList());
        detailed = parseStateMap(WorthType.class, "settings.detailed", false);

        addDefaults(RecalculateReason.class, "settings.perform-recalculate", true, Collections.emptyList());
        performRecalculate = parseStateMap(RecalculateReason.class, "settings.perform-recalculate", false);

        addDefaults(RecalculateReason.class, "settings.bypass-recalculate-delay", false, Arrays.asList(RecalculateReason.UNLOAD, RecalculateReason.CLAIM));
        bypassRecalculateDelay = parseStateMap(RecalculateReason.class, "settings.bypass-recalculate-delay", false);

        Map<String, Double> prices = ImmutableMap.of(
                "SLIME", 75_000.00,
                "SKELETON", 30_000.00,
                "ZOMBIE", 25_000.00
        );
        addDefaults("settings.spawner-prices", parseDefPrices(EntityType.class, prices));
        spawnerPrices = parsePriceMap(EntityType.class, "settings.spawner-prices", 0);

        prices = ImmutableMap.of(
                "EMERALD_BLOCK", 1_250.00,
                "DIAMOND_BLOCK", 1_000.00,
                "GOLD_BLOCK", 250.00,
                "IRON_BLOCK", 75.00,
                "COAL_BLOCK", 25.00
        );
        addDefaults("settings.block-prices", parseDefPrices(Material.class, prices));
        blockPrices = parsePriceMap(Material.class, "settings.block-prices", 0);

        // Update the configuration file if it is outdated.
        if (version < LATEST_VERSION) {
            // Update header and all config values.
            config.options().header(HEADER);
            config.options().copyDefaults(true);
            set("config-version", LATEST_VERSION);

            // Save the config.
            config.save(configFile);
            plugin.getLogger().info("Configuration file has been successfully updated.");
        }
    }
}
