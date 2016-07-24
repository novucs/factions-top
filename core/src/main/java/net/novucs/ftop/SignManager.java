package net.novucs.ftop;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class SignManager extends BukkitRunnable implements PluginService, Listener {

    private static final Pattern signRegex = Pattern.compile("\\[f(|actions)top\\]");
    private final FactionsTopPlugin plugin;
    private final Multimap<Integer, BlockPos> signs = HashMultimap.create();

    public SignManager(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    public void setSigns(Multimap<Integer, BlockPos> signs) {
        this.signs.clear();
        this.signs.putAll(signs);
    }

    @Override
    public void initialize() {
        int ticks = plugin.getSettings().getSignUpdateTicks();
        runTaskTimer(plugin, ticks, ticks);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void terminate() {
        cancel();
        HandlerList.unregisterAll(this);
    }

    @Override
    public void run() {
        List<FactionWorth> factions = plugin.getWorthManager().getOrderedFactions();

        for (Map.Entry<Integer, Collection<BlockPos>> entry : signs.asMap().entrySet()) {
            // Do nothing if rank is higher than factions size.
            if (entry.getKey() > factions.size()) continue;

            // Get the faction worth.
            FactionWorth worth = factions.get(entry.getKey());

            // Update all signs.
            for (BlockPos pos : entry.getValue()) {
                Block block = pos.getBlock(plugin.getServer());
                if (block == null || !(block.getState() instanceof Sign)) continue;

                Sign sign = (Sign) block.getState();
                sign.setLine(2, worth.getName());
                sign.setLine(3, plugin.getCurrencyFormat().format(worth.getTotalWorth()));
                sign.update();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void registerSign(SignChangeEvent event) {
        // Do nothing if the sign should not be registered.
        if (!event.getPlayer().hasPermission("factionstop.sign.create") ||
                !signRegex.matcher(event.getLine(0).toLowerCase()).find()) {
            return;
        }

        // Attempt to parse the rank for this sign.
        int rank;
        try {
            rank = Integer.parseInt(event.getLine(1));
        } catch (NumberFormatException e) {
            event.getPlayer().sendMessage(ChatColor.RED + "Invalid rank number on line 2!");
            event.setLine(0, ChatColor.DARK_RED + "[FactionsTop]");
            return;
        }

        event.setLine(0, ChatColor.DARK_BLUE + "[FactionsTop]");
        event.setLine(1, "#" + Math.min(rank, 1));
        saveSign(BlockPos.of(event.getBlock()), Math.max(rank - 1, 1));
    }

    private void saveSign(BlockPos pos, int rank) {
        signs.put(rank, pos);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.getDatabaseManager().saveSign(pos, rank);
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save a sign to the database!");
                plugin.getLogger().log(Level.SEVERE, "The error is as follows: ", e);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void unregisterSign(BlockBreakEvent event) {
        // Do nothing if block is not a registered sign.
        BlockPos pos = BlockPos.of(event.getBlock());
        if (!signs.containsValue(pos)) {
            return;
        }

        if (!(event.getBlock().getState() instanceof Sign)) {
            removeSign(pos);
            return;
        }

        if (!event.getPlayer().hasPermission("factionstop.sign.break")) {
            event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission.");
            event.setCancelled(true);
            return;
        }

        removeSign(pos);
    }

    private void removeSign(BlockPos pos) {
        signs.values().remove(pos);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.getDatabaseManager().removeSign(pos);
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to remove a sign from the database!");
                plugin.getLogger().log(Level.SEVERE, "The error is as follows: ", e);
            }
        });
    }
}
