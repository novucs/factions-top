package net.novucs.ftop.hook;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import net.novucs.ftop.hook.replacer.LastReplacer;
import net.novucs.ftop.hook.replacer.RankReplacer;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class MVdWPlaceholderAPIHook implements PlaceholderHook {

    private final Plugin plugin;
    private final Function<Integer, String> rankReplacer;
    private final Supplier<String> lastReplacer;

    public MVdWPlaceholderAPIHook(Plugin plugin, Function<Integer, String> rankReplacer, Supplier<String> lastReplacer) {
        this.plugin = plugin;
        this.rankReplacer = rankReplacer;
        this.lastReplacer = lastReplacer;
    }

    @Override
    public boolean initialize(List<Integer> enabledRanks) {
        boolean updated = PlaceholderAPI.registerPlaceholder(plugin, "factionstop_rank:last", new LastReplacer(lastReplacer));

        for (int rank : enabledRanks) {
            RankReplacer replacer = new RankReplacer(rankReplacer, rank);
            if (PlaceholderAPI.registerPlaceholder(plugin, "factionstop_rank:" + rank, replacer)) {
                updated = true;
            }
        }

        return updated;
    }
}
