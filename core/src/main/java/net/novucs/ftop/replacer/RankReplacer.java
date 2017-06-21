package net.novucs.ftop.replacer;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.entity.FactionWorth;

import java.util.List;
import java.util.function.Function;

public class RankReplacer implements Function<Integer, String> {

    private final FactionsTopPlugin plugin;

    public RankReplacer(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String apply(Integer rank) {
        List<FactionWorth> factions = plugin.getWorthManager().getOrderedFactions();

        if (factions.size() >= rank) {
            return factions.get(rank - 1).getName();
        }

        return plugin.getSettings().getPlaceholdersFactionNotFound();
    }
}
