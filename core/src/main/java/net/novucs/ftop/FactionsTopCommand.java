package net.novucs.ftop;

import mkremins.fanciful.FancyMessage;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.*;

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
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("factionstop.reload")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission.");
                return true;
            }

            plugin.loadSettings();
            sender.sendMessage(ChatColor.YELLOW + "FactionsTop settings have been successfully reloaded.");
            sender.sendMessage(ChatColor.YELLOW + "New faction worth values will take a while to register.");
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
        int maxPage = Math.max(factions.size() / entries, 1);
        page = Math.max(1, Math.min(maxPage, page));

        FancyMessage header = new FancyMessage("________.[ ").color(ChatColor.GOLD)
                .then("Top Factions ").color(ChatColor.DARK_GREEN)
                .then("[<] ").color(page == 1 ? ChatColor.GRAY : ChatColor.AQUA);

        if (page != 1) {
            header.command("/ftop " + (page - 1))
                    .tooltip(ChatColor.LIGHT_PURPLE + "Command: " + ChatColor.AQUA + "/f top " + (page - 1));
        }

        header.then(page + "/" + maxPage + " ").color(ChatColor.GOLD)
                .then("[>] ").color(page == maxPage ? ChatColor.GRAY : ChatColor.AQUA);

        if (page != maxPage) {
            header.command("/ftop " + (page + 1))
                    .tooltip(ChatColor.LIGHT_PURPLE + "Command: " + ChatColor.AQUA + "/f top " + (page + 1));
        }

        header.then("].________").color(ChatColor.GOLD);
        header.send(sender);

        if (factions.size() == 0) {
            sender.sendMessage(ChatColor.YELLOW + "No entries to be displayed.");
            return;
        }

        int spacer = entries * --page;
        ListIterator<FactionWorth> it = factions.listIterator(spacer);
        for (int i = 0; i < entries; i++) {
            if (!it.hasNext()) return;

            FactionWorth worth = it.next();
            ChatColor relationColor = getRelationColor(sender, worth.getFactionId());
            List<String> tooltip = getTooltip(worth);

            FancyMessage message = new FancyMessage((i + 1 + spacer) + ". ").color(ChatColor.YELLOW)
                    .then(worth.getName() + " ").color(relationColor).tooltip(tooltip)
                    .then(plugin.getCurrencyFormat().format(worth.getTotalWorth())).color(ChatColor.AQUA).tooltip(tooltip);

            message.send(sender);
        }
    }

    private List<String> getTooltip(FactionWorth worth) {
        List<String> tooltip = new ArrayList<>();
        addTooltip(tooltip, worth.getSpawners(), "Spawner");
        addTooltip(tooltip, worth.getMaterials(), "");
        return tooltip;
    }

    private <T extends Enum<T>> void addTooltip(List<String> tooltip, Map<T, Integer> counter, String added) {
        counter.forEach((type, count) -> {
            if (count > 0) {
                tooltip.add(ChatColor.DARK_AQUA + format(type.name()) + " " + added + ": " + ChatColor.AQUA + count);
            }
        });
    }

    private String format(String enumName) {
        char[] chars = enumName.toCharArray();
        boolean firstLetter = true;
        for (int i = 0; i < chars.length; i++) {
            if (firstLetter) {
                chars[i] = Character.toUpperCase(chars[i]);
                firstLetter = false;
            } else {
                chars[i] = Character.toLowerCase(chars[i]);
            }

            if (chars[i] == '_') {
                chars[i] = ' ';
                firstLetter = true;
            }
        }
        return new String(chars);
    }

    private ChatColor getRelationColor(CommandSender sender, String factionId) {
        return sender instanceof Player ? plugin.getFactionsHook().getRelation((Player) sender, factionId) : ChatColor.WHITE;
    }
}
