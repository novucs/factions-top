package net.novucs.ftop.hook;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.api.UserDoesNotExistException;
import net.ess3.api.Economy;
import net.ess3.api.events.UserBalanceUpdateEvent;
import net.novucs.ftop.WorthType;
import net.novucs.ftop.hook.event.FactionEconomyEvent;
import net.novucs.ftop.hook.event.PlayerEconomyEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.plugin.Plugin;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EssentialsEconomyHook implements EconomyHook, Listener {

    private final Plugin plugin;
    private final FactionsHook factionsHook;
    private boolean playerEnabled;
    private boolean factionEnabled;
    private IEssentials essentials = null;

    public EssentialsEconomyHook(Plugin plugin, FactionsHook factionsHook) {
        this.plugin = plugin;
        this.factionsHook = factionsHook;
    }

    @Override
    public void initialize() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        essentials = (IEssentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
    }

    @Override
    public void terminate() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public void setPlayerEnabled(boolean enabled) {
        playerEnabled = enabled;
    }

    @Override
    public void setFactionEnabled(boolean enabled) {
        factionEnabled = enabled;
    }

    @Override
    public double getBalance(Player player) {
        try {
            return Economy.getMoneyExact(player.getName()).doubleValue();
        } catch (UserDoesNotExistException e) {
            return 0;
        }
    }

    @Override
    public Map<WorthType, Double> getBalances(String factionId, List<UUID> members) {
        Map<WorthType, Double> target = new EnumMap<>(WorthType.class);

        try {
            target.put(WorthType.FACTION_BALANCE, Economy.getMoneyExact("faction_" + factionId).doubleValue());
        } catch (UserDoesNotExistException ignore) {
        }

        double playerBalance = 0;
        for (UUID playerId : members) {
            User user = essentials.getUser(playerId);
            if (user != null) {
                playerBalance += user.getMoney().doubleValue();
            }
        }

        target.put(WorthType.PLAYER_BALANCE, playerBalance);
        return target;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEconomyEvent(UserBalanceUpdateEvent event) {
        double oldBalance = event.getOldBalance().doubleValue();
        double newBalance = event.getNewBalance().doubleValue();

        Player player = event.getPlayer();
        if (!player.isOnline() && player.getName().startsWith("faction_")) {
            String factionId = player.getName().substring(8).replace("_", "-");
            if (factionsHook.isFaction(factionId)) {
                if (factionEnabled) {
                    callEvent(new FactionEconomyEvent(factionId, oldBalance, newBalance));
                }
                return;
            }
        }

        try {
            if (Economy.isNPC(player.getName())) {
                return;
            }
        } catch (UserDoesNotExistException ignore) {
        }

        if (playerEnabled) {
            callEvent(new PlayerEconomyEvent(player, oldBalance, newBalance));
        }
    }

    private void callEvent(Event event) {
        plugin.getServer().getPluginManager().callEvent(event);
    }
}
