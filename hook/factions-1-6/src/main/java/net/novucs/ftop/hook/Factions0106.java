package net.novucs.ftop.hook;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.massivecraft.factions.*;
import com.massivecraft.factions.event.*;
import com.massivecraft.factions.zcore.persist.MemoryBoard;
import com.massivecraft.factions.zcore.persist.MemoryFactions;
import net.novucs.ftop.entity.ChunkPos;
import net.novucs.ftop.hook.event.FactionDisbandEvent;
import net.novucs.ftop.hook.event.FactionRenameEvent;
import net.novucs.ftop.hook.event.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class Factions0106 extends FactionsHook {

    private Map<FLocation, String> flocationIds;
    private Map<String, Faction> factions;

    public Factions0106(Plugin plugin) {
        super(plugin);
    }

    @Override
    public String getFactionAt(String worldName, int chunkX, int chunkZ) {
        Faction faction = Board.getInstance().getFactionAt(new FLocation(worldName, chunkX, chunkZ));
        return faction == null ? Factions.getInstance().getNone().getId() : faction.getId();
    }

    @Override
    public void initialize() {
        try {
            Field flocationIdsField = MemoryBoard.class.getDeclaredField("flocationIds");
            flocationIdsField.setAccessible(true);
            flocationIds = (Map<FLocation, String>) flocationIdsField.get(Board.getInstance());
            flocationIdsField.setAccessible(false);

            Field factionsField = MemoryFactions.class.getDeclaredField("factions");
            factionsField.setAccessible(true);
            factions = (Map<String, Faction>) factionsField.get(Factions.getInstance());
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
        return FPlayers.getInstance().getByPlayer(player).getFaction().getId();
    }

    @Override
    public String getFactionName(String factionId) {
        return Factions.getInstance().getFactionById(factionId).getTag();
    }

    @Override
    public boolean isFaction(String factionId) {
        return Factions.getInstance().getFactionById(factionId) != null;
    }

    @Override
    public ChatColor getRelation(Player player, String factionId) {
        FPlayer fplayer = FPlayers.getInstance().getByPlayer(player);
        Faction faction = Factions.getInstance().getFactionById(factionId);
        return fplayer.getFaction().getRelationTo(faction).getColor();
    }

    @Override
    public String getOwnerName(String factionId) {
        Faction faction = Factions.getInstance().getFactionById(factionId);

        if (faction == null) {
            return null;
        }

        FPlayer owner = faction.getFPlayerAdmin();
        return owner == null ? null : owner.getName();
    }

    @Override
    public List<UUID> getMembers(String factionId) {
        return Factions.getInstance().getFactionById(factionId).getFPlayers().stream()
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
    public void onDisband(com.massivecraft.factions.event.FactionDisbandEvent event) {
        String factionId = event.getFaction().getId();
        String factionName = event.getFaction().getTag();
        callEvent(new FactionDisbandEvent(factionId, factionName));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRename(com.massivecraft.factions.event.FactionRenameEvent event) {
        String factionId = event.getFaction().getId();
        String oldName = event.getfPlayer().getFaction().getTag();
        String newName = event.getFactionTag();
        callEvent(new FactionRenameEvent(factionId, oldName, newName));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClaim(LandClaimEvent event) {
        Faction faction = Board.getInstance().getFactionAt(event.getLocation());
        Multimap<String, ChunkPos> claims = HashMultimap.create();
        claims.put(faction.getId(), getChunkPos(event.getLocation()));
        callEvent(new FactionClaimEvent(event.getFaction().getId(), claims));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClaim(LandUnclaimEvent event) {
        Multimap<String, ChunkPos> claims = HashMultimap.create();
        claims.put(event.getFaction().getId(), getChunkPos(event.getLocation()));
        callEvent(new FactionClaimEvent(Factions.getInstance().getNone().getId(), claims));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClaim(LandUnclaimAllEvent event) {
        Multimap<String, ChunkPos> claims = HashMultimap.create();
        for (FLocation location : event.getFaction().getClaimOwnership().keySet()) {
            claims.put(event.getFaction().getId(), getChunkPos(location));
        }
        callEvent(new FactionClaimEvent(Factions.getInstance().getNone().getId(), claims));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(FPlayerJoinEvent event) {
        Player player = event.getfPlayer().getPlayer();
        String factionId = event.getFaction().getId();
        callEvent(new FactionJoinEvent(factionId, player));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeave(FPlayerLeaveEvent event) {
        Player player = event.getfPlayer().getPlayer();
        String factionId = event.getFaction().getId();
        if (player != null) {
            callEvent(new FactionLeaveEvent(factionId, player));
        }
    }

    private Set<ChunkPos> getChunkPos(Set<FLocation> locations) {
        return locations.stream().map(this::getChunkPos).collect(Collectors.toSet());
    }

    private ChunkPos getChunkPos(FLocation location) {
        return ChunkPos.of(location.getWorldName(), (int) location.getX(), (int) location.getZ());
    }
}
