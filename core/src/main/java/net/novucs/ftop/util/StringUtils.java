package net.novucs.ftop.util;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.Settings;
import net.novucs.ftop.WorthType;
import net.novucs.ftop.entity.FactionWorth;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class StringUtils {

    private StringUtils() {
    }

    public static String format(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static List<String> format(List<String> messages) {
        return messages.stream()
                .map(StringUtils::format)
                .collect(Collectors.toList());
    }

    public static ChatColor getRelationColor(FactionsTopPlugin plugin, CommandSender sender, String factionId) {
        if (!(sender instanceof Player)) {
            return ChatColor.WHITE;
        }

        ChatColor relationColor = plugin.getFactionsHook().getRelation((Player) sender, factionId);
        return relationColor == null ? ChatColor.WHITE : relationColor;
    }

    public static String insertPlaceholders(Replacer replacer, String key, String message) {
        int index = message.indexOf('{' + key + ':');
        if (index < 0) {
            return message;
        }

        String first = message.substring(0, index);
        String next = message.substring(index + key.length() + 2);

        index = next.indexOf('}');

        if (index < 0) {
            return first + insertPlaceholders(replacer, key, next);
        }

        return first + replacer.replace(next.substring(0, index)) + insertPlaceholders(replacer, key, next.substring(index + 1));
    }

    public static String insertPlaceholders(Settings settings, FactionWorth worth, String message) {
        message = insertPlaceholders((s) -> {
            double value = worth.getWorth(GenericUtils.parseEnum(WorthType.class, s).orElse(null));
            return settings.getCurrencyFormat().format(value);
        }, "worth", message);

        message = insertPlaceholders((s) -> {
            int count = worth.getSpawners().getOrDefault(GenericUtils.parseEnum(EntityType.class, s).orElse(null), 0);
            return settings.getCountFormat().format(count);
        }, "count:spawner", message);

        message = insertPlaceholders((s) -> {
            int count = worth.getMaterials().getOrDefault(GenericUtils.parseEnum(Material.class, s).orElse(null), 0);
            return settings.getCountFormat().format(count);
        }, "count:material", message);

        return message;
    }

    public static List<String> insertPlaceholders(Settings settings, FactionWorth worth, List<String> messages) {
        return messages.stream()
                .map(message -> insertPlaceholders(settings, worth, message))
                .collect(Collectors.toList());
    }

    public static String replace(String message, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        return message;
    }

    public static List<String> replace(List<String> messages, Map<String, String> placeholders) {
        return messages.stream()
                .map(message -> replace(message, placeholders))
                .collect(Collectors.toList());
    }
}
