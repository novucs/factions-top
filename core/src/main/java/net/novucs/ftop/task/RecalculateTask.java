package net.novucs.ftop.task;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.PluginService;
import net.novucs.ftop.RecalculateReason;
import net.novucs.ftop.entity.ChunkPos;
import org.bukkit.Chunk;

import java.util.Stack;

public class RecalculateTask implements PluginService, Runnable {

    private final FactionsTopPlugin plugin;
    private final Stack<ChunkPos> toRecalculate = new Stack<>();
    private int taskId;

    public RecalculateTask(FactionsTopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        if (!isRunning()) {
            toRecalculate.addAll(plugin.getFactionsHook().getClaims());
            taskId = plugin.getServer().getScheduler().runTaskTimer(plugin, this, 1, 1).getTaskId();
            plugin.getServer().broadcastMessage(plugin.getSettings().getRecalculationStartMessage());
        } else {
            throw new IllegalStateException("Recalculation task is already running");
        }
    }

    @Override
    public void terminate() {
        if (isRunning()) {
            toRecalculate.clear();
            plugin.getServer().getScheduler().cancelTask(taskId);
            plugin.getServer().broadcastMessage(plugin.getSettings().getRecalculationStopMessage());
        } else {
            throw new IllegalStateException("No recalculation task was running");
        }
    }

    public boolean isRunning() {
        return !toRecalculate.isEmpty();
    }

    @Override
    public void run() {
        int counter = plugin.getSettings().getRecalculateChunksPerTick();

        while (isRunning()) {
            if (counter-- <= 0) {
                break;
            }

            ChunkPos pos = toRecalculate.pop();
            Chunk chunk = pos.getChunk(plugin.getServer());
            if (chunk != null && chunk.load()) {
                plugin.getWorthManager().recalculate(chunk, RecalculateReason.COMMAND);
            }
        }

        if (!isRunning()) {
            plugin.getWorthManager().updateAllFactions();
            plugin.getServer().getScheduler().cancelTask(taskId);
            plugin.getServer().broadcastMessage(plugin.getSettings().getRecalculationFinishMessage());
        }
    }
}
