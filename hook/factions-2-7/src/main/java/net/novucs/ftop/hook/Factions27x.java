package net.novucs.ftop.hook;

import com.massivecraft.factions.entity.*;
import com.massivecraft.factions.event.EventFactionsDisband;
import com.massivecraft.factions.event.EventFactionsNameChange;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

public class Factions27x extends FactionsHook {

    public Factions27x(Plugin plugin) {
        super(plugin);
    }

    @Override
    public String getFactionAt(String worldName, int chunkX, int chunkZ) {
        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(worldName, chunkX, chunkZ));
        return faction.getFlag(MFlag.getFlagPeaceful()) ? null : faction.getId();
    }

    @Override
    public String getFactionName(String factionId) {
        return FactionColl.get().get(factionId).getName();
    }

    @Override
    public ChatColor getRelation(Player player, String factionId) {
        MPlayer mplayer = MPlayer.get(player);
        Faction faction = Faction.get(factionId);
        return mplayer.getFaction().getRelationTo(faction).getColor();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDisband(EventFactionsDisband event) {
        String factionId = event.getFactionId();
        String factionName = event.getFaction().getName();
        getPlugin().getServer().getPluginManager().callEvent(new FactionDisbandEvent(factionId, factionName));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRename(EventFactionsNameChange event) {
        String factionId = event.getFaction().getId();
        String oldName = event.getFaction().getName();
        String newName = event.getNewName();
        getPlugin().getServer().getPluginManager().callEvent(new FactionRenameEvent(factionId, oldName, newName));
    }
}
