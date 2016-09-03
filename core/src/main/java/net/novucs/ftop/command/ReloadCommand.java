package net.novucs.ftop.command;

import com.google.common.collect.ImmutableList;
import mkremins.fanciful.FancyMessage;
import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.PluginService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor, PluginService {

    private static final FancyMessage MESSAGE = new FancyMessage("Configuration successfully reloaded. ")
            .color(ChatColor.YELLOW)
            .then("Hover for details.")
            .color(ChatColor.LIGHT_PURPLE)
            .tooltip(ImmutableList.of(
                    ChatColor.YELLOW + "Without a full server resynchronization,",
                    ChatColor.YELLOW + "new worth values will take a while",
                    ChatColor.YELLOW + "to register. If you have modified any",
                    ChatColor.YELLOW + "worth values and wish to see immediate",
                    ChatColor.YELLOW + "changes, please type this:",
                    ChatColor.LIGHT_PURPLE + "/ftoprec"
            ));
    private final FactionsTopPlugin plugin;

    public ReloadCommand(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        plugin.getCommand("ftopreload").setExecutor(this);
    }

    @Override
    public void terminate() {
        plugin.getCommand("ftopreload").setExecutor(null);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("factionstop.reload")) {
            sender.sendMessage(plugin.getSettings().getPermissionMessage());
            return true;
        }

        plugin.loadSettings();
        MESSAGE.send(sender);
        return true;
    }
}
