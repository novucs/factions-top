package net.novucs.ftop.command;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.PluginService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor, PluginService {

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
        sender.sendMessage(ChatColor.YELLOW + "FactionsTop settings have been successfully reloaded.");
        sender.sendMessage(ChatColor.YELLOW + "New faction worth values will take a while to register.");
        return true;
    }
}
