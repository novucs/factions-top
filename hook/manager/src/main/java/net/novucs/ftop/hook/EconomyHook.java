package net.novucs.ftop.hook;

import net.novucs.ftop.PluginService;
import net.novucs.ftop.WorthType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface EconomyHook extends PluginService {

    void setPlayerEnabled(boolean enabled);

    void setFactionEnabled(boolean enabled);

    double getBalance(Player player);

    Map<WorthType, Double> getBalances(String factionId, List<UUID> members);

}
