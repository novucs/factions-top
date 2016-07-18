package net.novucs.ftop.hook;

import net.novucs.ftop.ChunkPos;
import net.novucs.ftop.PluginService;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public abstract class FactionsHook implements Listener, PluginService {

    private final Plugin plugin;

    public FactionsHook(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public void initialize() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void terminate() {
        HandlerList.unregisterAll(this);
    }

    public String getFactionAt(ChunkPos pos) {
        return getFactionAt(pos.getWorld(), pos.getX(), pos.getZ());
    }

    public String getFactionAt(Block block) {
        return getFactionAt(block.getWorld().getName(), block.getChunk().getX(), block.getChunk().getZ());
    }

    public abstract String getFactionAt(String worldName, int chunkX, int chunkZ);

    public abstract String getFaction(Player player);

    public abstract String getFactionName(String factionId);

    public abstract boolean isFaction(String factionId);

    public abstract ChatColor getRelation(Player player, String factionId);
}
