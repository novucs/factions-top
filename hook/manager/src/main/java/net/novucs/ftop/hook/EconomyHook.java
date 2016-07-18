package net.novucs.ftop.hook;

import net.novucs.ftop.PluginService;
import org.bukkit.entity.Player;

public interface EconomyHook extends PluginService {

    double getBalance(Player player);

}
