package com.example.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;
import java.util.Optional;

public class ExamplePlugin extends JavaPlugin {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may execute this command");
            return true;
        }

        Player player = (Player) sender;
        Optional<String> id = FactionsTopApi.getId(player);
        Optional<Integer> rank = id.flatMap(FactionsTopApi::getRank);
        Optional<Double> worth = id.flatMap(FactionsTopApi::getWorth).map(FactionsTopApi.Worth::getTotal);

        if (!rank.isPresent() || !worth.isPresent()) {
            sender.sendMessage("Your faction is currently not ranked");
            return true;
        }

        DecimalFormat df = new DecimalFormat("$#,###.##");
        sender.sendMessage(String.format("Your faction is ranked #%d with %s", rank.get(), df.format(worth.get())));
        return true;
    }
}
