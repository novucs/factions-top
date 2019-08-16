package net.novucs.ftop.hook;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.event.EventFactionsChunksChange;
import com.massivecraft.factions.event.EventFactionsDisband;
import com.massivecraft.factions.event.EventFactionsMembershipChange;
import com.massivecraft.factions.event.EventFactionsNameChange;
import com.massivecraft.massivecore.ps.PS;
import com.massivecraft.massivecore.store.SenderEntity;
import net.novucs.ftop.entity.ChunkPos;
import net.novucs.ftop.hook.event.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Factions0207 extends FactionsHook {

    public Factions0207(Plugin plugin) {
        super(plugin);
    }

    @Override
    public String getFactionAt(String worldName, int chunkX, int chunkZ) {
        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(worldName, chunkX, chunkZ));
        return faction == null ? Factions.ID_NONE : faction.getId();
    }

    @Override
    public String getFaction(Player player) {
        return MPlayer.get(player).getFaction().getId();
    }

    @Override
    public String getFactionName(String factionId) {
        return FactionColl.get().get(factionId).getName();
    }

    @Override
    public boolean isFaction(String factionId) {
        return FactionColl.get().get(factionId) != null;
    }

    @Override
    public ChatColor getRelation(Player player, String factionId) {
        MPlayer mplayer = MPlayer.get(player);
        Faction faction = Faction.get(factionId);
        return mplayer.getFaction().getRelationTo(faction).getColor();
    }

    @Override
    public String getOwnerName(String factionId) {
        MPlayer owner = FactionColl.get().get(factionId).getLeader();
        return owner == null ? null : owner.getName();
    }

    @Override
    public List<UUID> getMembers(String factionId) {
        return FactionColl.get().get(factionId).getMPlayers().stream()
                .map(SenderEntity::getUuid)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChunkPos> getClaims() {
        List<ChunkPos> target = new LinkedList<>();
        for (Set<PS> ps : BoardColl.get().getFactionToChunks().values()) {
            target.addAll(psToChunkPos(ps));
        }
        return target;
    }

    @Override
    public Set<String> getFactionIds() {
        return FactionColl.get().getId2entity().keySet();
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMembershipChange(EventFactionsMembershipChange event) {
        Player player = event.getMPlayer().getPlayer();
        if (player == null) return;

        Faction oldFaction = event.getMPlayer().getFaction();
        Faction newFaction = event.getNewFaction();
        String oldFactionId = event.getMPlayer().getFaction().getId();
        String newFactionId = oldFaction == newFaction ? FactionColl.get().getNone().getId() : newFaction.getId();
        callEvent(new FactionLeaveEvent(oldFactionId, player));
        callEvent(new FactionJoinEvent(newFactionId, player));
    }

    private Set<ChunkPos> psToChunkPos(Set<PS> positions) {
        return positions.stream().map(this::psToChunkPos).collect(Collectors.toSet());
    }

    private ChunkPos psToChunkPos(PS ps) {
        return ChunkPos.of(ps.getWorld(), ps.getChunkX(), ps.getChunkZ());
    }
}
