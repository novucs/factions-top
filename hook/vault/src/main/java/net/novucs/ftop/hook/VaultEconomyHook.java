package net.novucs.ftop.hook;

import net.milkbowl.vault.economy.Economy;
import net.novucs.ftop.WorthType;
import net.novucs.ftop.hook.event.FactionEconomyEvent;
import net.novucs.ftop.hook.event.PlayerEconomyEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class VaultEconomyHook extends BukkitRunnable implements EconomyHook, Listener {

    private final Plugin plugin;
    private final Set<String> factionIds;
    private final Map<UUID, Double> playerBalances = new HashMap<>();
    private final Map<String, Double> factionBalances = new HashMap<>();
    private boolean enabled;
    private boolean playerEnabled;
    private boolean factionEnabled;
    private int liquidUpdateTicks;
    private Economy economy;

    public VaultEconomyHook(Plugin plugin, Set<String> factionIds) {
        this.plugin = plugin;
        this.factionIds = factionIds;
    }

    public void setLiquidUpdateTicks(int liquidUpdateTicks) {
        this.liquidUpdateTicks = liquidUpdateTicks;
    }

    @Override
    public void initialize() {
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null || (economy = rsp.getProvider()) == null) {
            plugin.getLogger().warning("No economy provider for Vault found!");
            plugin.getLogger().warning("Economy support by Vault is now disabled.");
            return;
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        runTaskTimer(plugin, liquidUpdateTicks, liquidUpdateTicks);
        enabled = true;
    }

    @Override
    public void terminate() {
        if (enabled) {
            HandlerList.unregisterAll(this);
            cancel();
            enabled = false;
        }
    }

    @Override
    public void setPlayerEnabled(boolean enabled) {
        playerEnabled = enabled;
    }

    @Override
    public void setFactionEnabled(boolean enabled) {
        factionEnabled = enabled;
    }

    @Override
    public double getBalance(Player player) {
        return economy == null ? 0 : economy.getBalance(player);
    }

    @Override
    public Map<WorthType, Double> getBalances(String factionId, List<UUID> members) {
        Map<WorthType, Double> target = new EnumMap<>(WorthType.class);
        if (economy == null) return target;

        target.put(WorthType.FACTION_BALANCE, economy.getBalance(factionId));

        double playerBalance = 0;
        for (UUID playerId : members) {
            playerBalance += economy.getBalance(plugin.getServer().getOfflinePlayer(playerId));
        }

        target.put(WorthType.PLAYER_BALANCE, playerBalance);
        return target;
    }

    @Override
    public void run() {
        // Tick players if enabled.
        if (playerEnabled) {
            tickPlayers();
        }

        // Tick factions if enabled.
        if (factionEnabled) {
            tickFactions();
        }
    }

    private void tickPlayers() {
        Double oldBalance;
        Double newBalance;
        UUID playerId;

        // Iterate through every player on the server.
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            // Get their previous and current balances.
            playerId = player.getUniqueId();
            oldBalance = playerBalances.get(playerId);
            newBalance = economy.getBalance(player);

            // Add new balance if player is not already added to the cache.
            if (oldBalance == null) {
                playerBalances.put(playerId, newBalance);
                continue;
            }

            // Call PlayerEconomyEvent if their balance has changed.
            if (oldBalance.doubleValue() != newBalance.doubleValue()) {
                playerBalances.put(playerId, newBalance);
                callEvent(new PlayerEconomyEvent(player, oldBalance, newBalance));
            }
        }
    }

    private void tickFactions() {
        Double oldBalance;
        Double newBalance;

        // Iterate through every faction on the server.
        for (String factionId : factionIds) {
            // Get their previous and current balances.
            oldBalance = factionBalances.getOrDefault(factionId, 0d);
            newBalance = economy.getBalance(factionId);

            // Add new balance if faction is not already added to the cache.
            if (oldBalance == null) {
                factionBalances.put(factionId, newBalance);
                continue;
            }

            // Call FactionEconomyEvent if their balance has changed.
            if (oldBalance.doubleValue() != newBalance.doubleValue()) {
                factionBalances.put(factionId, newBalance);
                callEvent(new FactionEconomyEvent(factionId, oldBalance, newBalance));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void addPlayer(PlayerJoinEvent event) {
        playerBalances.put(event.getPlayer().getUniqueId(), economy.getBalance(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void clearPlayerData(PlayerQuitEvent event) {
        playerBalances.remove(event.getPlayer().getUniqueId());
    }

    private void callEvent(Event event) {
        plugin.getServer().getPluginManager().callEvent(event);
    }
}
