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

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

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
        if (!sender.hasPermission("factionstop.use")) {
            sender.sendMessage(plugin.getSettings().getPermissionMessage());
            return true;
        }

        if (args.length == 0) {
            sendTop(sender, 0);
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("factionstop.reload")) {
                sender.sendMessage(plugin.getSettings().getPermissionMessage());
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
        event.setMessage("/" + attemptRebind(event.getMessage().substring(1)));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCommand(ServerCommandEvent event) {
        event.setCommand(attemptRebind(event.getCommand()));
    }

    private String attemptRebind(String command) {
        for (String alias : plugin.getSettings().getCommandAliases()) {
            if (command.startsWith(alias)) {
                return command.replaceFirst(alias, "ftop");
            }
        }

        return command;
    }

    private void sendTop(CommandSender sender, int page) {
        // Do not attempt to send hook worth if page requested is beyond the limit.
        int entries = plugin.getSettings().getFactionsPerPage();
        List<FactionWorth> factions = plugin.getWorthManager().getOrderedFactions();
        int maxPage = Math.max((int) Math.ceil((double) factions.size() / entries), 1);
        page = Math.max(1, Math.min(maxPage, page));

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{page:back}", String.valueOf(page - 1));
        placeholders.put("{page:this}", String.valueOf(page));
        placeholders.put("{page:next}", String.valueOf(page + 1));
        placeholders.put("{page:last}", String.valueOf(maxPage));

        ButtonMessage back = plugin.getSettings().getBackButtonMessage();
        ButtonMessage next = plugin.getSettings().getNextButtonMessage();

        String backMsg = page == 1 ? back.getDisabled() : back.getEnabled();
        String backCmd = page == 1 ? null : "/ftop " + (page - 1);
        List<String> backTooltip = replace(back.getTooltip(), placeholders);

        String nextMsg = page == maxPage ? next.getDisabled() : next.getEnabled();
        String nextCmd = page == maxPage ? null : "/ftop " + (page + 1);
        List<String> nextTooltip = replace(next.getTooltip(), placeholders);

        if (!plugin.getSettings().getHeaderMessage().isEmpty()) {
            String headerString = replace(plugin.getSettings().getHeaderMessage(), placeholders);
            FancyMessage header = build(headerString, backMsg, backCmd, backTooltip, nextMsg, nextCmd, nextTooltip);
            header.send(sender);
        }

        if (factions.size() == 0) {
            sender.sendMessage(plugin.getSettings().getNoEntriesMessage());
            return;
        }

        int spacer = entries * --page;
        ListIterator<FactionWorth> it = factions.listIterator(spacer);
        for (int i = 0; i < entries; i++) {
            if (!it.hasNext()) break;

            FactionWorth worth = it.next();

            int spawnerCount = 0;
            for (EntityType spawner : worth.getSpawners().keySet()) {
                spawnerCount += worth.getSpawners().get(spawner);
            }

            Map<String, String> worthPlaceholders = new HashMap<>(placeholders);
            worthPlaceholders.put("{rank}", Integer.toString(i + 1));
            worthPlaceholders.put("{relcolor}", "" + ChatColor.COLOR_CHAR + getRelationColor(sender, worth.getFactionId()).getChar());
            worthPlaceholders.put("{faction}", worth.getName());
            worthPlaceholders.put("{worth:total}", plugin.getSettings().getCurrencyFormat().format(worth.getTotalWorth()));
            worthPlaceholders.put("{count:total:spawner}", String.valueOf(spawnerCount));

            String bodyMessage = insertPlaceholders(worth, replace(plugin.getSettings().getBodyMessage(), worthPlaceholders));
            List<String> tooltip = insertPlaceholders(worth, replace(plugin.getSettings().getBodyTooltip(), worthPlaceholders));

            FancyMessage message = new FancyMessage(bodyMessage).tooltip(tooltip);
            message.send(sender);
        }

        if (!plugin.getSettings().getFooterMessage().isEmpty()) {
            String footerString = replace(plugin.getSettings().getFooterMessage(), placeholders);
            FancyMessage footer = build(footerString, backMsg, backCmd, backTooltip, nextMsg, nextCmd, nextTooltip);
            footer.send(sender);
        }
    }

    private String insertPlaceholders(Replacer replacer, String key, String message) {
        int index = message.indexOf('{' + key + ':');
        if (index < 0) {
            return message;
        }

        String first = message.substring(0, index);
        String next = message.substring(index + key.length() + 2);

        index = next.indexOf('}');

        if (index < 0) {
            return first + insertPlaceholders(replacer, key, next);
        }

        return first + replacer.replace(next.substring(0, index)) + insertPlaceholders(replacer, key, next.substring(index + 1));
    }

    private String insertPlaceholders(FactionWorth worth, String message) {
        message = insertPlaceholders((s) -> {
            double value = worth.getWorth(StringUtils.parseEnum(WorthType.class, s).orElse(null));
            return plugin.getSettings().getCurrencyFormat().format(value);
        }, "worth", message);

        message = insertPlaceholders((s) -> {
            int count = worth.getSpawners().getOrDefault(StringUtils.parseEnum(EntityType.class, s).orElse(null), 0);
            return plugin.getSettings().getCountFormat().format(count);
        }, "count:spawner", message);

        message = insertPlaceholders((s) -> {
            int count = worth.getMaterials().getOrDefault(StringUtils.parseEnum(Material.class, s).orElse(null), 0);
            return plugin.getSettings().getCountFormat().format(count);
        }, "count:material", message);

        return message;
    }

    private List<String> insertPlaceholders(FactionWorth worth, List<String> messages) {
        return messages.stream()
                .map(message -> insertPlaceholders(worth, message))
                .collect(Collectors.toList());
    }

    private String replace(String message, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        return message;
    }

    private List<String> replace(List<String> messages, Map<String, String> placeholders) {
        return messages.stream()
                .map(message -> replace(message, placeholders))
                .collect(Collectors.toList());
    }

    private FancyMessage build(String message, String backText, String backCmd, List<String> backTooltip,
                               String nextText, String nextCmd, List<String> nextTooltip) {
        FancyMessage fancyMessage = new FancyMessage("");

        while (!message.isEmpty()) {
            int backIndex = message.indexOf("{button:back}");
            int nextIndex = message.indexOf("{button:next}");

            backIndex = backIndex == -1 ? Integer.MAX_VALUE : backIndex;
            nextIndex = nextIndex == -1 ? Integer.MAX_VALUE : nextIndex;

            if (backIndex < nextIndex && backIndex != Integer.MAX_VALUE) {
                fancyMessage.then(message.substring(0, backIndex)).then(backText);

                if (backCmd != null) {
                    fancyMessage.command(backCmd).tooltip(backTooltip);
                }

                message = message.substring(backIndex + 13);
            } else if (nextIndex < backIndex && nextIndex != Integer.MAX_VALUE) {
                fancyMessage.then(message.substring(0, nextIndex)).then(nextText);

                if (nextCmd != null) {
                    fancyMessage.command(nextCmd).tooltip(nextTooltip);
                }

                message = message.substring(nextIndex + 13);
            } else {
                fancyMessage.then(message);
                break;
            }
        }

        return fancyMessage;
    }

    private ChatColor getRelationColor(CommandSender sender, String factionId) {
        return sender instanceof Player ? plugin.getFactionsHook().getRelation((Player) sender, factionId) : ChatColor.WHITE;
    }
}
