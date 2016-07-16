package net.novucs.ftop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

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
    }

    @Override
    public void terminate() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendTop(sender, 0);
        }
        return true;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
    }

    private void sendTop(CommandSender sender, int page) {
        // Do not attempt to send factions worth if page requested is beyond the limit.
        int entries = plugin.getSettings().getFactionsPerPage();
        List<FactionWorth> factions = plugin.getWorthManager().getOrderedFactions();
        if (factions.size() < entries * page) {
            return;
        }

        ListIterator<FactionWorth> it = factions.listIterator(entries * page);
        for (int i = 0; i < entries; i++) {
            FactionWorth worth = it.next();

        }
    }
}
