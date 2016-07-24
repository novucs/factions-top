package net.novucs.ftop;

import mkremins.fanciful.FancyMessage;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.text.DecimalFormat;
import java.util.List;
import java.util.ListIterator;

public class FactionsTopCommand implements CommandExecutor, Listener, PluginService {

    private final FactionsTopPlugin plugin;

    public FactionsTopCommand(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getPluginCommand("ftop").setExecutor(this);
    }

    @Override
    public void terminate() {
        HandlerList.unregisterAll(this);
        plugin.getServer().getPluginCommand("ftop").setExecutor(null);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendTop(sender, 0);
        } else {
            sendTop(sender, NumberUtils.toInt(args[0]));
        }
        return true;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (onCommand(event.getPlayer(), event.getMessage().substring(1))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCommand(ServerCommandEvent event) {
        if (onCommand(event.getSender(), event.getCommand())) {
            event.setCancelled(true);
        }
    }

    private boolean onCommand(CommandSender sender, String command) {
        for (String alias : plugin.getSettings().getCommandAliases()) {
            if (command.startsWith(alias)) {
                if (!sender.hasPermission("factionstop.use")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission.");
                    return true;
                }

                int page = NumberUtils.toInt(command.replaceFirst(alias, "").split(" ")[0]);
                sendTop(sender, page);
                return true;
            }
        }
        return false;
    }

    private void sendTop(CommandSender sender, int page) {
        // Do not attempt to send hook worth if page requested is beyond the limit.
        int entries = plugin.getSettings().getFactionsPerPage();
        List<FactionWorth> factions = plugin.getWorthManager().getOrderedFactions();
        int maxPage = factions.size() / entries;
        page = Math.max(0, Math.min(maxPage, page));

        FancyMessage header = new FancyMessage("________.[ ").color(ChatColor.GOLD)
                .then("Top Factions ").color(ChatColor.DARK_GREEN)
                .then("[<] ").color(page == 0 ? ChatColor.GRAY : ChatColor.AQUA).command("/ftop " + (page - 1))
                .then((page + 1) + "/" + (maxPage + 1) + " ").color(ChatColor.GOLD)
                .then("[>] ").color(page == maxPage ? ChatColor.GRAY : ChatColor.AQUA).command("/ftop " + (page + 1))
                .then("].________").color(ChatColor.GOLD);
        header.send(sender);

        if (factions.size() == 0) {
            for (int i = 0; i < entries; i++) {
                sender.sendMessage("");
            }
        }

        ListIterator<FactionWorth> it = factions.listIterator(entries * page);
        for (int i = 0; i < entries; i++) {
            if (!it.hasNext()) {
                for (int j = 0; j < i - entries; j++) {
                    sender.sendMessage("");
                }
                return;
            }

            FactionWorth worth = it.next();
            ChatColor relationColor = getRelationColor(sender, worth.getFactionId());
            FancyMessage message = new FancyMessage((i + 1) + ". ").color(ChatColor.YELLOW)
                    .then(worth.getName()).color(relationColor)
                    .then(plugin.getCurrencyFormat().format(worth.getTotalWorth())).color(ChatColor.AQUA);
            message.send(sender);
        }
    }

    private ChatColor getRelationColor(CommandSender sender, String factionId) {
        return sender instanceof Player ? plugin.getFactionsHook().getRelation((Player) sender, factionId) : ChatColor.WHITE;
    }
}
