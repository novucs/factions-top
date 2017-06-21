package net.novucs.ftop.hook;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.novucs.ftop.entity.ChunkPos;
import net.novucs.ftop.hook.event.FactionClaimEvent;
import net.novucs.ftop.hook.event.FactionDisbandEvent;
import net.novucs.ftop.hook.event.FactionJoinEvent;
import net.novucs.ftop.hook.event.FactionLeaveEvent;
import net.novucs.ftop.hook.event.FactionRenameEvent;
import net.redstoneore.legacyfactions.FLocation;
import net.redstoneore.legacyfactions.Factions;
import net.redstoneore.legacyfactions.entity.Board;
import net.redstoneore.legacyfactions.entity.FPlayer;
import net.redstoneore.legacyfactions.entity.FPlayerColl;
import net.redstoneore.legacyfactions.entity.Faction;
import net.redstoneore.legacyfactions.entity.FactionColl;
import net.redstoneore.legacyfactions.entity.persist.memory.MemoryBoard;
import net.redstoneore.legacyfactions.entity.persist.memory.MemoryFactions;
import net.redstoneore.legacyfactions.event.EventFactionsChange;
import net.redstoneore.legacyfactions.event.EventFactionsLandChange;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class LegacyFactions0103 extends FactionsHook {

    private Map<FLocation, String> flocationIds;
    private Map<String, Faction> factions;

    public LegacyFactions0103(Plugin plugin) {
        super(plugin);
    }

    @Override
    public String getFactionAt(String worldName, int chunkX, int chunkZ) {
        Faction faction = Board.get().getFactionAt(new FLocation(worldName, chunkX, chunkZ));
        return faction.getId();
    }

    @Override
    public void initialize() {
        try {
            Field flocationIdsField = MemoryBoard.class.getDeclaredField("flocationIds");
            flocationIdsField.setAccessible(true);
            flocationIds = (Map<FLocation, String>) flocationIdsField.get(Board.get());
            flocationIdsField.setAccessible(false);

            Field factionsField = MemoryFactions.class.getDeclaredField("factions");
            factionsField.setAccessible(true);
            factions = (Map<String, Faction>) factionsField.get(Factions.get());
            factionsField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            getPlugin().getLogger().severe("Factions version found is incompatible!");
            getPlugin().getServer().getPluginManager().disablePlugin(getPlugin());
            return;
        }

        super.initialize();
    }

    @Override
    public String getFaction(Player player) {
        return FPlayerColl.get(player).getFaction().getId();
    }

    @Override
    public String getFactionName(String factionId) {
        return FactionColl.get().getFactionById(factionId).getTag();
    }

    @Override
    public boolean isFaction(String factionId) {
        return FactionColl.get().getFactionById(factionId) != null;
    }

    @Override
    public ChatColor getRelation(Player player, String factionId) {
        FPlayer fplayer = FPlayerColl.get(player);
        Faction faction = FactionColl.get().getFactionById(factionId);
        return fplayer.getFaction().getRelationTo(faction).getColor();
    }

    @Override
    public String getOwnerName(String factionId) {
        FPlayer owner = FactionColl.get().getFactionById(factionId).getFPlayerAdmin();
        return owner == null ? null : owner.getName();
    }

    @Override
    public List<UUID> getMembers(String factionId) {
        return FactionColl.get().getFactionById(factionId).getFPlayers().stream()
                .map(fplayer -> UUID.fromString(fplayer.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ChunkPos> getClaims() {
        List<ChunkPos> target = new LinkedList<>();
        target.addAll(getChunkPos(flocationIds.keySet()));
        return target;
    }

    @Override
    public Set<String> getFactionIds() {
        return factions.keySet();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDisband(FactionDisbandEvent event) {
        String factionId = event.getFactionId();
        String factionName = event.getName();
        callEvent(new FactionDisbandEvent(factionId, factionName));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRename(FactionRenameEvent event) {
        String factionId = event.getFactionId();
        String oldName = event.getOldName();
        String newName = event.getNewName();
        callEvent(new FactionRenameEvent(factionId, oldName, newName));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClaim(EventFactionsLandChange event) {
        String factionId = event.getFPlayer().getFactionId();

        if (event.getCause() == EventFactionsLandChange.LandChangeCause.Claim) {
            Multimap<String, ChunkPos> claims = HashMultimap.create();

            for (Map.Entry<FLocation, Faction> transaction : event.getTransactions().entrySet()) {
                claims.put(transaction.getValue().getId(), getChunkPos(transaction.getKey()));
            }

            callEvent(new FactionClaimEvent(factionId, claims));
        } else {
            for (Map.Entry<FLocation, Faction> transaction : event.getTransactions().entrySet()) {
                Multimap<String, ChunkPos> claims = HashMultimap.create();
                claims.put(factionId, getChunkPos(transaction.getKey()));
                callEvent(new FactionClaimEvent(transaction.getValue().getId(), claims));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChangeFaction(EventFactionsChange event) {
        Player player = event.getFPlayer().getPlayer();
        callEvent(new FactionJoinEvent(event.getFactionNew().getId(), player));
        callEvent(new FactionLeaveEvent(event.getFactionOld().getId(), player));
    }

    private Set<ChunkPos> getChunkPos(Set<FLocation> locations) {
        return locations.stream().map(this::getChunkPos).collect(Collectors.toSet());
    }

    private ChunkPos getChunkPos(FLocation location) {
        return ChunkPos.of(location.getWorldName(), (int) location.getX(), (int) location.getZ());
    }

    private void callEvent(Event event) {
        getPlugin().getServer().getPluginManager().callEvent(event);
    }
}
