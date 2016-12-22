package net.novucs.ftop.listener;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.PluginService;
import net.novucs.ftop.entity.FactionWorth;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener, PluginService {

    private static final String RANK_PLACEHOLDER = "{FACTION_RANK}";
    private final FactionsTopPlugin plugin;

    public ChatListener(FactionsTopPlugin plugin) {
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void replacePlaceholders(AsyncPlayerChatEvent event) {
        if (!event.getFormat().contains(RANK_PLACEHOLDER)) {
            return;
        }

        String factionId = plugin.getFactionsHook().getFaction(event.getPlayer());
        String format = event.getFormat();

        if (plugin.getSettings().getIgnoredFactionIds().contains(factionId)) {
            format = format.replace(RANK_PLACEHOLDER, "");
            event.setFormat(format);
            return;
        }

        FactionWorth worth = plugin.getWorthManager().getWorth(factionId);
        int rank = plugin.getWorthManager().getOrderedFactions().indexOf(worth);
        format = format.replace(RANK_PLACEHOLDER, String.valueOf(rank));
        event.setFormat(format);
    }
}
