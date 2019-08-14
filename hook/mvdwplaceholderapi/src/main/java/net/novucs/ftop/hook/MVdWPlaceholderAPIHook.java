package net.novucs.ftop.hook;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import net.novucs.ftop.hook.replacer.LastReplacer;
import net.novucs.ftop.hook.replacer.PlayerReplacer;
import net.novucs.ftop.hook.replacer.RankReplacer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class MVdWPlaceholderAPIHook implements PlaceholderHook {

    private final Plugin plugin;
    private final Function<Player, String> playerReplacer;
    private final Function<Integer, String> rankReplacer;
    private final Supplier<String> lastReplacer;

    public MVdWPlaceholderAPIHook(Plugin plugin,
                                  Function<Player, String> playerReplacer,
                                  Function<Integer, String> rankReplacer,
                                  Supplier<String> lastReplacer) {
        this.plugin = plugin;
        this.playerReplacer = playerReplacer;
        this.rankReplacer = rankReplacer;
        this.lastReplacer = lastReplacer;
    }

    @Override
    public boolean initialize(List<Integer> enabledRanks) {
        LastReplacer lastReplacer = new LastReplacer(this.lastReplacer);
        boolean updated = PlaceholderAPI.registerPlaceholder(plugin, "factionstop_name:last", lastReplacer);

        for (int rank : enabledRanks) {
            RankReplacer replacer = new RankReplacer(rankReplacer, rank);
            if (PlaceholderAPI.registerPlaceholder(plugin, "factionstop_name:" + rank, replacer)) {
                updated = true;
            }
        }

        PlayerReplacer playerReplacer = new PlayerReplacer(this.playerReplacer);
        if (PlaceholderAPI.registerPlaceholder(plugin, "factionstop_rank:player", playerReplacer)) {
            updated = true;
        }

        return updated;
    }
}
