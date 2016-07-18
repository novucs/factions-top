package net.novucs.ftop.hook;

import com.earth2me.essentials.api.UserDoesNotExistException;
import net.ess3.api.Economy;
import net.ess3.api.events.UserBalanceUpdateEvent;
import net.novucs.ftop.hook.event.FactionEconomyEvent;
import net.novucs.ftop.hook.event.PlayerEconomyEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.plugin.Plugin;

public class EssentialsEconomyHook implements EconomyHook, Listener {

    private final Plugin plugin;
    private final FactionsHook factionsHook;

    public EssentialsEconomyHook(Plugin plugin, FactionsHook factionsHook) {
        this.plugin = plugin;
        this.factionsHook = factionsHook;
    }

    @Override
    public void initialize() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void terminate() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public double getBalance(Player player) {
        try {
            return Economy.getMoneyExact(player.getName()).doubleValue();
        } catch (UserDoesNotExistException e) {
            return 0;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEconomyEvent(UserBalanceUpdateEvent event) {
        double oldBalance = event.getOldBalance().doubleValue();
        double newBalance = event.getNewBalance().doubleValue();

        Player player = event.getPlayer();
        if (player.getName().startsWith("faction_")) {
            String factionId = player.getName().substring(8);
            if (factionsHook.isFaction(factionId)) {
                callEvent(new FactionEconomyEvent(factionId, oldBalance, newBalance));
            }
        }

        try {
            if (Economy.isNPC(player.getName())) {
                return;
            }
        } catch (UserDoesNotExistException ignore) {
        }

        callEvent(new PlayerEconomyEvent(player, oldBalance, newBalance));
    }

    private void callEvent(Event event) {
        plugin.getServer().getPluginManager().callEvent(event);
    }
}
