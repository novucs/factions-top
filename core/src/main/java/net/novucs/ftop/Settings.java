package net.novucs.ftop;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zaxxer.hikari.HikariConfig;
import net.novucs.ftop.entity.ButtonMessage;
import net.novucs.ftop.gui.GuiLayout;
import net.novucs.ftop.gui.element.GuiElement;
import net.novucs.ftop.gui.element.GuiElementType;
import net.novucs.ftop.gui.element.GuiFactionList;
import net.novucs.ftop.hook.VaultEconomyHook;
import net.novucs.ftop.util.GenericUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static net.novucs.ftop.util.StringUtils.format;

public class Settings {

    private static final int LATEST_VERSION = 7;

    private static final ImmutableList<String> WORTH_HOVER = ImmutableList.of(
            "&e&l-- General --",
            "&dTotal Worth: &b{worth:total}",
            "&dBlock Worth: &b{worth:block}",
            "&dChest Worth: &b{worth:chest}",
            "&dSpawner Worth: &b{worth:spawner}",
            "&dPlayer Balances: &b{worth:player_balance}",
            "&dFaction Bank: &b{worth:faction_balance}",
            "",
            "&e&l-- Spawners --",
            "&dSlime: &b{count:spawner:slime}",
            "&dSkeleton: &b{count:spawner:skeleton}",
            "&dZombie: &b{count:spawner:zombie}",
            "",
            "&e&l-- Materials --",
            "&dEmerald Block: &b{count:material:emerald_block}",
            "&dDiamond Block: &b{count:material:diamond_block}",
            "&dGold Block: &b{count:material:gold_block}",
            "&dIron Block: &b{count:material:iron_block}",
            "&dCoal Block: &b{count:material:coal_block}"
    );

    private static final ImmutableList<ImmutableMap<?, ?>> GUI_LAYOUT = ImmutableList.of(
            ImmutableMap.of(
                    "type", "button_back",
                    "enabled", ImmutableMap.of(
                            "text", "&bBack",
                            "lore", new ArrayList<>(),
                            "material", "wool",
                            "data", 5
                    ),
                    "disabled", ImmutableMap.of(
                            "text", "&7Back",
                            "lore", new ArrayList<>(),
                            "material", "wool",
                            "data", 14
                    )),
            ImmutableMap.of(
                    "type", "faction_list",
                    "faction-count", 7,
                    "fill-empty", true,
                    "text", "&e{rank}. {relcolor}{faction} &b{worth:total}",
                    "lore", new ArrayList<>(WORTH_HOVER)
            ),
            ImmutableMap.of(
                    "type", "button_next",
                    "enabled", ImmutableMap.of(
                            "text", "&bNext",
                            "lore", new ArrayList<>(),
                            "material", "wool",
                            "data", 5
                    ),
                    "disabled", ImmutableMap.of(
                            "text", "&7Next",
                            "lore", new ArrayList<>(),
                            "material", "wool",
                            "data", 14
                    )
            )
    );

    private final FactionsTopPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    // Message settings.
    private DecimalFormat countFormat;
    private DecimalFormat currencyFormat;
    private ButtonMessage backButtonMessage;
    private ButtonMessage nextButtonMessage;
    private String headerMessage;
    private String noEntriesMessage;
    private String bodyMessage;
    private List<String> bodyTooltip;
    private String footerMessage;
    private String permissionMessage;
    private String recalculationStartMessage;
    private String recalculationFinishMessage;
    private String recalculationStopMessage;

    // GUI settings.
    private List<String> guiCommandAliases;
    private int guiLineCount;
    private String guiInventoryName;
    private GuiLayout guiLayout;

    // General settings.
    private List<String> commandAliases;
    private List<String> ignoredFactionIds;
    private boolean disableChestEvents;
    private int factionsPerPage;
    private int signUpdateTicks;
    private int liquidUpdateTicks;
    private int chunkQueueSize;
    private int recalculateChunksPerTick;
    private long chunkRecalculateMillis;
    private boolean chatEnabled;
    private String chatRankPlaceholder;
    private String chatRankFound;
    private String chatRankNotFound;
    private String placeholdersFactionNotFound;
    private List<Integer> placeholdersEnabledRanks;
    private long databasePersistInterval;
    private boolean databasePersistFactions;
    private HikariConfig hikariConfig;
    private Map<WorthType, Boolean> enabled;
    private Map<RecalculateReason, Boolean> performRecalculate;
    private Map<RecalculateReason, Boolean> bypassRecalculateDelay;
    private Map<EntityType, Double> spawnerPrices;
    private Map<Material, Double> blockPrices;

    public Settings(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    public DecimalFormat getCountFormat() {
        return countFormat;
    }

    public DecimalFormat getCurrencyFormat() {
        return currencyFormat;
    }

    public ButtonMessage getBackButtonMessage() {
        return backButtonMessage;
    }

    public ButtonMessage getNextButtonMessage() {
        return nextButtonMessage;
    }

    public String getHeaderMessage() {
        return headerMessage;
    }

    public String getNoEntriesMessage() {
        return noEntriesMessage;
    }

    public String getBodyMessage() {
        return bodyMessage;
    }

    public List<String> getBodyTooltip() {
        return bodyTooltip;
    }

    public String getFooterMessage() {
        return footerMessage;
    }

    public String getPermissionMessage() {
        return permissionMessage;
    }

    public String getRecalculationStartMessage() {
        return recalculationStartMessage;
    }

    public String getRecalculationFinishMessage() {
        return recalculationFinishMessage;
    }

    public String getRecalculationStopMessage() {
        return recalculationStopMessage;
    }

    public List<String> getGuiCommandAliases() {
        return guiCommandAliases;
    }

    public int getGuiLineCount() {
        return guiLineCount;
    }

    public String getGuiInventoryName() {
        return guiInventoryName;
    }

    public GuiLayout getGuiLayout() {
        return guiLayout;
    }

    public List<String> getCommandAliases() {
        return commandAliases;
    }

    public List<String> getIgnoredFactionIds() {
        return ignoredFactionIds;
    }

    public boolean isDisableChestEvents() {
        return disableChestEvents;
    }

    public int getFactionsPerPage() {
        return factionsPerPage;
    }

    public int getSignUpdateTicks() {
        return signUpdateTicks;
    }

    public int getLiquidUpdateTicks() {
        return liquidUpdateTicks;
    }

    public int getChunkQueueSize() {
        return chunkQueueSize;
    }

    public int getRecalculateChunksPerTick() {
        return recalculateChunksPerTick;
    }

    public long getChunkRecalculateMillis() {
        return chunkRecalculateMillis;
    }

    public boolean isChatEnabled() {
        return chatEnabled;
    }

    public String getChatRankPlaceholder() {
        return chatRankPlaceholder;
    }

    public String getChatRankFound() {
        return chatRankFound;
    }

    public String getChatRankNotFound() {
        return chatRankNotFound;
    }

    public String getPlaceholdersFactionNotFound() {
        return placeholdersFactionNotFound;
    }

    public List<Integer> getPlaceholdersEnabledRanks() {
        return placeholdersEnabledRanks;
    }

    public long getDatabasePersistInterval() {
        return databasePersistInterval;
    }

    public boolean isDatabasePersistFactions() {
        return databasePersistFactions;
    }

    public HikariConfig getHikariConfig() {
        return hikariConfig;
    }

    public boolean isEnabled(WorthType worthType) {
        return enabled.getOrDefault(worthType, false);
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
            Optional<T> parsed = GenericUtils.parseEnum(type, name);
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
            section.addDefault(target.name().toLowerCase(), exempt.contains(target) != def);
        }
    }

    private <T extends Enum<T>> EnumMap<T, Double> parsePriceMap(Class<T> type, String key, double def) {
        EnumMap<T, Double> target = new EnumMap<>(type);
        ConfigurationSection section = getOrDefaultSection(key);
        for (String name : section.getKeys(false)) {
            // Warn user if unable to parse enum.
            Optional<T> parsed = GenericUtils.parseEnum(type, name);
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
        prices.forEach((type, price) -> section.addDefault(type.name().toLowerCase(), price));
    }

    private <T extends Enum<T>> EnumMap<T, Double> parseDefPrices(Class<T> type, Map<String, Double> def) {
        EnumMap<T, Double> target = new EnumMap<>(type);
        def.forEach((name, price) -> {
            GenericUtils.parseEnum(type, name).ifPresent(t ->
                    target.put(t, price));
        });
        return target;
    }

    private GuiLayout loadGuiLayout() {
        config.addDefault("gui-settings.layout", GUI_LAYOUT);

        List<Map<?, ?>> layout = config.getMapList("gui-settings.layout");
        List<GuiElement> elements = new ArrayList<>(layout.size());

        for (Map<?, ?> element : layout) {
            GenericUtils.getEnum(GuiElementType.class, element, "type").ifPresent(guiElementType ->
                    elements.add(guiElementType.getParser().parse(element)));
        }

        int factionsPerPage = 0;

        for (GuiElement element : elements) {
            if (element instanceof GuiFactionList) {
                factionsPerPage += ((GuiFactionList) element).getFactionCount();
            }
        }

        return new GuiLayout(ImmutableList.copyOf(elements), factionsPerPage);
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

    private ButtonMessage getButtonMessage(String path, ButtonMessage def) {
        String enabled = format(getString(path + ".enabled", def.getEnabled()));
        String disabled = format(getString(path + ".disabled", def.getDisabled()));
        List<String> tooltip = format(getList(path + ".tooltip", def.getTooltip(), String.class));
        return new ButtonMessage(enabled, disabled, tooltip);
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
        countFormat = new DecimalFormat(getString("messages.count-format", "#,###"));
        currencyFormat = new DecimalFormat(getString("messages.currency-format", "$#,###.##"));
        backButtonMessage = getButtonMessage("messages.button-back",
                new ButtonMessage("&b[<]", "&7[<]", Collections.singletonList("&dCommand: &b/f top {page:back}")));
        nextButtonMessage = getButtonMessage("messages.button-next",
                new ButtonMessage("&b[>]", "&7[>]", Collections.singletonList("&dCommand: &b/f top {page:next}")));
        headerMessage = format(getString("messages.header",
                "&6_______.[ &2Top Factions {button:back} &6{page:this}/{page:last} {button:next} &6]._______"));
        noEntriesMessage = format(getString("messages.no-entries", "&eNo entries to be displayed."));
        bodyMessage = format(getString("messages.body.text", "&e{rank}. {relcolor}{faction} &b{worth:total}"));
        bodyTooltip = format(getList("messages.body.tooltip", new ArrayList<>(WORTH_HOVER), String.class));
        footerMessage = format(getString("messages.footer", ""));
        permissionMessage = format(getString("messages.permission", "&cYou do not have permission."));
        recalculationStartMessage = format(getString("messages.recalculation-start",
                "&eAll faction totals are being resynchronized"));
        recalculationFinishMessage = format(getString("messages.recalculation-finish",
                "&eResynchronization of faction totals complete"));
        recalculationStopMessage = format(getString("messages.recalculation-stop",
                "&eResynchronization of faction totals stopped"));

        guiCommandAliases = getList("gui-settings.command-aliases", Collections.singletonList("f topgui"), String.class);
        guiLineCount = getInt("gui-settings.line-count", 1);
        guiInventoryName = format(getString("gui-settings.inventory-name", "&lTop Factions | Page {page:this}"));
        guiLayout = loadGuiLayout();

        commandAliases = getList("settings.command-aliases", Collections.singletonList("f top"), String.class);
        ignoredFactionIds = getList("settings.ignored-faction-ids",
                Arrays.asList("none", "safezone", "warzone", "0", "-1", "-2"), String.class);
        disableChestEvents = getBoolean("settings.disable-chest-events", false);
        factionsPerPage = getInt("settings.factions-per-page", 9);
        signUpdateTicks = getInt("settings.sign-update-ticks", 1);
        liquidUpdateTicks = getInt("settings.liquid-update-ticks", 100);
        if (plugin.getEconomyHook() instanceof VaultEconomyHook) {
            ((VaultEconomyHook) plugin.getEconomyHook()).setLiquidUpdateTicks(liquidUpdateTicks);
        }
        chunkQueueSize = getInt("settings.chunk-queue-size", 200);
        recalculateChunksPerTick = getInt("settings.recalculate-chunks-per-tick", 50);
        chunkRecalculateMillis = getLong("settings.chunk-recalculate-millis", 120_000);

        chatEnabled = getBoolean("settings.chat.enabled", false);
        chatRankPlaceholder = getString("settings.chat.rank-placeholder", "{factions_top_rank}");
        chatRankFound = format(getString("settings.chat.rank-found", "&2[&e#{rank}&2]" ));
        chatRankNotFound = format(getString("settings.chat.rank-not-found", ""));

        placeholdersFactionNotFound = format(getString("settings.placeholders.faction-not-found", "-"));
        placeholdersEnabledRanks = getList("settings.placeholders.enabled-ranks", Arrays.asList(1, 2, 3), Integer.class);

        // Do not reload hikari configuration if already loaded.
        if (hikariConfig == null) {
            hikariConfig = loadHikariConfig();
        }

        databasePersistInterval = Math.max(1000, getLong("settings.database.persist-interval", 60_000));
        databasePersistFactions = getBoolean("settings.database.persist-factions", false);

        addDefaults(WorthType.class, "settings.enabled", true, Collections.emptyList());
        enabled = parseStateMap(WorthType.class, "settings.enabled", false);

        if (plugin.getEconomyHook() != null) {
            plugin.getEconomyHook().setFactionEnabled(isEnabled(WorthType.FACTION_BALANCE));
            plugin.getEconomyHook().setPlayerEnabled(isEnabled(WorthType.PLAYER_BALANCE));
        }

        addDefaults(RecalculateReason.class, "settings.perform-recalculate", true, Collections.emptyList());
        performRecalculate = parseStateMap(RecalculateReason.class, "settings.perform-recalculate", false);

        addDefaults(RecalculateReason.class, "settings.bypass-recalculate-delay", false,
                Arrays.asList(RecalculateReason.COMMAND, RecalculateReason.UNLOAD, RecalculateReason.CLAIM));
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
            config.options().header(getDocumentation());
            config.options().copyDefaults(true);
            set("config-version", LATEST_VERSION);

            // Save the config.
            config.save(configFile);
            plugin.getLogger().info("Configuration file has been successfully updated.");
        }
    }

    public String getDocumentation() {
        Scanner scanner = new Scanner(plugin.getResource("readme.txt")).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }
}
