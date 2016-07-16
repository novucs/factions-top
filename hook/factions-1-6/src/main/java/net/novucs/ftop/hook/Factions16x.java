package net.novucs.ftop.hook;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

public class Factions16x extends FactionsHook {

    public Factions16x(Plugin plugin) {
        super(plugin);
    }

    @Override
    public String getFactionAt(String worldName, int chunkX, int chunkZ) {
        Faction faction = Board.getInstance().getFactionAt(new FLocation(worldName, chunkX, chunkZ));
        return faction.isPeaceful() ? null : faction.getId();
    }

    @Override
    public String getFactionName(String factionId) {
        return Factions.getInstance().getFactionById(factionId).getTag();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDisband(com.massivecraft.factions.event.FactionDisbandEvent event) {
        String factionId = event.getFaction().getId();
        String factionName = event.getFaction().getTag();
        getPlugin().getServer().getPluginManager().callEvent(new FactionDisbandEvent(factionId, factionName));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRename(com.massivecraft.factions.event.FactionRenameEvent event) {
        String factionId = event.getFaction().getId();
        String oldName = event.getfPlayer().getFaction().getTag();
        String newName = event.getFactionTag();
        getPlugin().getServer().getPluginManager().callEvent(new FactionRenameEvent(factionId, oldName, newName));
    }
}
