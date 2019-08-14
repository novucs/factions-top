package net.novucs.ftop.hook.replacer;

import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import org.bukkit.entity.Player;

import java.util.function.Function;

public class PlayerReplacer implements PlaceholderReplacer {

    private final Function<Player, String> playerReplacer;

    public PlayerReplacer(Function<Player, String> playerReplacer) {
        this.playerReplacer = playerReplacer;
    }

    @Override
    public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
        return playerReplacer.apply(event.getPlayer());
    }
}
