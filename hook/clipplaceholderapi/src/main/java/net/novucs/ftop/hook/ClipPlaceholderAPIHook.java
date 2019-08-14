package net.novucs.ftop.hook;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClipPlaceholderAPIHook implements PlaceholderHook {

    private final Plugin plugin;
    private final Function<Player, String> playerReplacer;
    private final Function<Integer, String> rankReplacer;
    private final Supplier<String> lastReplacer;

    public ClipPlaceholderAPIHook(Plugin plugin,
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
        return PlaceholderAPI.registerPlaceholderHook("factionstop", new me.clip.placeholderapi.PlaceholderHook() {
            @Override
            public String onPlaceholderRequest(Player player, String identifier) {
                if ("name:last".equals(identifier)) {
                    return lastReplacer.get();
                } else if ("rank:player".equals(identifier)) {
                    return playerReplacer.apply(player);
                } else if (identifier.startsWith("name:")) {
                    String[] split = identifier.split(":");
                    if (split.length > 1) {
                        try {
                            int rank = Integer.parseInt(split[1]);
                            return rankReplacer.apply(rank);
                        } catch (NumberFormatException ignored) {}
                    }
                }
                return null;
            }
        });
    }
}
