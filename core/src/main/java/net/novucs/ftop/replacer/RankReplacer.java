package net.novucs.ftop.replacer;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.entity.FactionWorth;
import net.novucs.ftop.util.SortedSplayTree;

import java.util.function.Function;

public class RankReplacer implements Function<Integer, String> {

    private final FactionsTopPlugin plugin;

    public RankReplacer(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String apply(Integer rank) {
        SortedSplayTree<FactionWorth> factions = plugin.getWorthManager().getOrderedFactions();

        if (factions.size() >= rank) {
            return factions.byIndex(rank - 1).getName();
        }

        return plugin.getSettings().getPlaceholdersFactionNotFound();
    }
}
