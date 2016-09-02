package net.novucs.ftop.listener;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.PluginService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.List;
import java.util.regex.Pattern;

public class CommandListener implements Listener, PluginService {

    private static final Pattern VERSION_COMMAND = Pattern.compile("f( |)top( |)(v(ersion|))($| .*)");
    private static final Pattern RECALCULATE_COMMAND = Pattern.compile("f( |)top( |)(rec(alc(ulate|)|))($| .*)");
    private static final Pattern RELOAD_COMMAND = Pattern.compile("f( |)top( |)(r(eload|))($| .*)");
    private final FactionsTopPlugin plugin;

    public CommandListener(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void terminate() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        event.setMessage("/" + attemptRebind(event.getMessage().substring(1)));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCommand(ServerCommandEvent event) {
        event.setCommand(attemptRebind(event.getCommand()));
    }

    private String attemptRebind(String command) {
        if (VERSION_COMMAND.matcher(command).matches()) {
            return command.replaceFirst("f( |)top( |)(v(ersion|))", "ftopversion");
        }

        if (RECALCULATE_COMMAND.matcher(command).matches()) {
            return command.replaceFirst("f( |)top( |)(rec(alc(ulate|)|))", "ftoprecalculate");
        }

        if (RELOAD_COMMAND.matcher(command).matches()) {
            return command.replaceFirst("f( |)top( |)(r(eload|))", "ftopreload");
        }

        String newCommand = replaceFirst(command, "ftop", plugin.getSettings().getCommandAliases());
        if (newCommand != null) {
            return newCommand;
        }

        newCommand = replaceFirst(command, "ftopgui", plugin.getSettings().getGuiCommandAliases());
        if (newCommand != null) {
            return newCommand;
        }

        return command;
    }

    private String replaceFirst(String command, String replace, List<String> aliases) {
        for (String alias : aliases) {
            String newCommand = replaceFirst(command, alias, replace);
            if (newCommand != null) {
                return newCommand;
            }
        }
        return null;
    }

    private String replaceFirst(String command, String alias, String replace) {
        // Do nothing if not an alias.
        if (!command.startsWith(alias)) {
            return null;
        }

        // Do nothing if not followed by whitespace.
        if (command.length() > alias.length() && Character.SPACE_SEPARATOR != command.charAt(alias.length())) {
            return null;
        }

        return command.replaceFirst(alias, replace);
    }
}
