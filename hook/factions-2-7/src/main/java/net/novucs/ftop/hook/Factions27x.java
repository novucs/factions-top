package net.novucs.ftop.hook;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.massivecraft.factions.entity.*;
import com.massivecraft.factions.event.EventFactionsChunkChangeType;
import com.massivecraft.factions.event.EventFactionsChunksChange;
import com.massivecraft.factions.event.EventFactionsDisband;
import com.massivecraft.factions.event.EventFactionsNameChange;
import com.massivecraft.massivecore.ps.PS;
import net.novucs.ftop.ChunkPos;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Factions27x extends FactionsHook {

    public Factions27x(Plugin plugin) {
        super(plugin);
    }

    @Override
    public String getFactionAt(String worldName, int chunkX, int chunkZ) {
        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(worldName, chunkX, chunkZ));
        return faction.getId();
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
        callEvent(new FactionDisbandEvent(factionId, factionName));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRename(EventFactionsNameChange event) {
        String factionId = event.getFaction().getId();
        String oldName = event.getFaction().getName();
        String newName = event.getNewName();
        callEvent(new FactionRenameEvent(factionId, oldName, newName));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClaim(EventFactionsChunksChange event) {
        Multimap<String, ChunkPos> claims = HashMultimap.create();
        event.getOldFactionChunks().forEach((faction, chunks) -> claims.putAll(faction.getId(), psToChunkPos(chunks)));
        callEvent(new FactionClaimEvent(event.getNewFaction().getId(), claims));
    }

    private Set<ChunkPos> psToChunkPos(Set<PS> positions) {
        return positions.stream().map(this::psToChunkPos).collect(Collectors.toSet());
    }

    private ChunkPos psToChunkPos(PS ps) {
        return ChunkPos.of(ps.getWorld(), ps.getChunkX(), ps.getChunkZ());
    }

    private void callEvent(Event event) {
        getPlugin().getServer().getPluginManager().callEvent(event);
    }
}
