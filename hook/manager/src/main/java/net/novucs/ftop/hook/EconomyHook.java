package net.novucs.ftop.hook;

import com.google.common.collect.Table;
import net.novucs.ftop.PluginService;
import net.novucs.ftop.WorthType;
import org.bukkit.entity.Player;

public interface EconomyHook extends PluginService {

    void setPlayerEnabled(boolean enabled);

    void setFactionEnabled(boolean enabled);

    double getBalance(Player player);

    Table<String, WorthType, Double> getBalances();

}
