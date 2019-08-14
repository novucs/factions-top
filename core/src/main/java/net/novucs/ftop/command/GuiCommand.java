package net.novucs.ftop.command;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.PluginService;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuiCommand implements CommandExecutor, PluginService {

    private final FactionsTopPlugin plugin;

    public GuiCommand(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        plugin.getServer().getPluginCommand("ftopgui").setExecutor(this);
    }

    @Override
    public void terminate() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("factionstop.use")) {
            sender.sendMessage(plugin.getSettings().getPermissionMessage());
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players are allowed to see the GUI.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            plugin.getGuiManager().sendGui(player, 0);
        } else {
            plugin.getGuiManager().sendGui(player, NumberUtils.toInt(args[0]));
        }
        return true;
    }
}
