package net.novucs.ftop.command;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.PluginService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RecalculateCommand implements CommandExecutor, PluginService {

    private final FactionsTopPlugin plugin;

    public RecalculateCommand(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        plugin.getCommand("ftoprecalculate").setExecutor(this);
    }

    @Override
    public void terminate() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("factionstop.recalculate")) {
            sender.sendMessage(plugin.getSettings().getPermissionMessage());
            return true;
        }

        if (args.length > 0 && args[args.length - 1].startsWith("c")) {
            if (!plugin.getRecalculateTask().isRunning()) {
                sender.sendMessage(ChatColor.RED + "No recalculation task was running.");
                return true;
            }

            plugin.getRecalculateTask().terminate();
            return true;
        }

        if (plugin.getRecalculateTask().isRunning()) {
            sender.sendMessage(ChatColor.YELLOW + "A recalculation task is already running");
            return true;
        }

        plugin.getRecalculateTask().initialize();
        return true;
    }
}
