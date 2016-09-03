package net.novucs.ftop.hook;

import net.novucs.ftop.PluginService;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface EconomyHook extends PluginService {

    void setPlayerEnabled(boolean enabled);

    void setFactionEnabled(boolean enabled);

    double getBalance(Player player);

    double getBalance(UUID playerId);

    double getTotalBalance(List<UUID> playerIds);

    double getFactionBalance(String factionId);

}
