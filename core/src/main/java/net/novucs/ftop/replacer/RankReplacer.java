package net.novucs.ftop.replacer;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.entity.FactionWorth;
import net.novucs.ftop.util.SplaySet;

import java.util.function.Function;

public class RankReplacer implements Function<Integer, String> {

    private final FactionsTopPlugin plugin;

    public RankReplacer(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String apply(Integer rank) {
        SplaySet<FactionWorth> factions = plugin.getWorthManager().getOrderedFactions();

        if (rank > 0 && factions.size() >= rank) {
            return factions.byIndex(rank - 1).getName();
        }

        return plugin.getSettings().getPlaceholdersFactionNotFound();
    }
}
