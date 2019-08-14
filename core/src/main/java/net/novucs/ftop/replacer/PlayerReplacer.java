package net.novucs.ftop.replacer;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.entity.FactionWorth;
import org.bukkit.entity.Player;

import java.util.function.Function;

public class PlayerReplacer implements Function<Player, String> {

    private final FactionsTopPlugin plugin;

    public PlayerReplacer(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String apply(Player player) {
        String faction = plugin.getFactionsHook().getFaction(player);
        if (faction == null) {
            return plugin.getSettings().getPlaceholdersFactionNotFound();
        }

        FactionWorth factionWorth = plugin.getWorthManager().getWorth(faction);
        if (factionWorth == null) {
            return plugin.getSettings().getPlaceholdersFactionNotFound();
        }

        int rank = plugin.getWorthManager().getOrderedFactions().indexOf(factionWorth) + 1;
        if (rank <= 0) {
            return plugin.getSettings().getPlaceholdersFactionNotFound();
        }

        return Integer.toString(rank);
    }
}
