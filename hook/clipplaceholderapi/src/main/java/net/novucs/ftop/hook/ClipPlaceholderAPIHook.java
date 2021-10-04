package net.novucs.ftop.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClipPlaceholderAPIHook implements PlaceholderHook {

    private final Plugin plugin;
    private final Function<Player, String> playerReplacer;
    private final Function<Integer, String> rankReplacer;
    private final Supplier<String> lastReplacer;
    private HashSet<Integer> enabledRanks;

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
        this.enabledRanks = new HashSet<>(enabledRanks);
        return new PlaceholderExpansion() {

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
                            if (enabledRanks.contains(rank) || enabledRanks.isEmpty()) {
                                return rankReplacer.apply(rank);
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                return null;
            }


            @Override
            public boolean persist() {
                return true;
            }

            @Override
            public String getIdentifier() {
                return "factionstop";
            }

            @Override
            public String getAuthor() {
                return String.join(", ", plugin.getDescription().getAuthors());
            }

            @Override
            public String getVersion() {
                return plugin.getDescription().getVersion();
            }
        }.register();
    }
}
