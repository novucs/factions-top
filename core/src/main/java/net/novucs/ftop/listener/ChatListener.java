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

    private static final String RANK_PLACEHOLDER = "{rank}";
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
        // Do nothing if placeholder should not be applied.
        if (!plugin.getSettings().isChatEnabled()) {
            return;
        }

        String placeholder = plugin.getSettings().getChatRankPlaceholder();

        if (!event.getFormat().contains(placeholder)) {
            return;
        }

        String factionId = plugin.getFactionsHook().getFaction(event.getPlayer());
        String format = event.getFormat();

        // Set rank not found if player is in an ignored faction.
        if (plugin.getSettings().getIgnoredFactionIds().contains(factionId)) {
            format = format.replace(placeholder, plugin.getSettings().getChatRankNotFound());
            event.setFormat(format);
            return;
        }

        // Update chat format with rank found placeholder.
        FactionWorth worth = plugin.getWorthManager().getWorth(factionId);
        int rank = plugin.getWorthManager().getOrderedFactions().indexOf(worth) + 1;
        String rankFound = plugin.getSettings().getChatRankFound().replace(RANK_PLACEHOLDER, String.valueOf(rank));
        format = format.replace(placeholder, rankFound);
        event.setFormat(format);
    }
}
