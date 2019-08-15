package net.novucs.ftop.delayedspawners;

import com.google.common.collect.ImmutableMap;
import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.PluginService;
import net.novucs.ftop.RecalculateReason;
import net.novucs.ftop.WorthType;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.CreatureSpawner;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class DelayedSpawners implements PluginService {

	private static final long DELAY = TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS);

	private final LinkedHashMap<DelayedSpawner, Long> activationMap = new LinkedHashMap<>();
	private final FactionsTopPlugin plugin;

	public DelayedSpawners(FactionsTopPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void initialize() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::validateEntries, 20, 20);
	}

	public void queue(CreatureSpawner spawner) {
		plugin.getLogger().log(Level.INFO, "Spawner added to queue: " + spawner);
		activationMap.put(DelayedSpawner.of(spawner), System.currentTimeMillis() + DELAY);
	}

	public void queue(DelayedSpawner spawner, long time) {
		if (time < System.currentTimeMillis())
			return;

		plugin.getLogger().log(Level.INFO, "Spawner manually added to queue: " + spawner);
		activationMap.put(spawner, time);
	}

	public void removeFromQueue(CreatureSpawner spawner) {
		plugin.getLogger().log(Level.INFO, "Spawner removed from queue: " + spawner);
		activationMap.remove(DelayedSpawner.of(spawner));
	}

	public boolean isDelayed(CreatureSpawner spawner) {
		return Optional.ofNullable(activationMap.get(DelayedSpawner.of(spawner)))
				.map(done -> System.currentTimeMillis() < done).orElse(false);
	}

	private void validateEntries() {
		Iterator<Map.Entry<DelayedSpawner, Long>> it = activationMap.entrySet().iterator();

		while(it.hasNext()) {
			Map.Entry<DelayedSpawner, Long> entry = it.next();

			if (entry.getValue() > System.currentTimeMillis())
				break;

			validate(entry.getKey());
			it.remove();
		}
	}

	private void validate(DelayedSpawner spawner) {
		plugin.getLogger().log(Level.INFO, "Validating: " + spawner);

		Chunk chunk = spawner.getPos().getBlock(plugin.getServer()).getChunk();
		double price = plugin.getSettings().getSpawnerPrice(spawner.getEntityType());

		plugin.getWorthManager().add(chunk, RecalculateReason.DELAYED_SPAWNER, WorthType.SPAWNER, price,
				ImmutableMap.of(), ImmutableMap.of(spawner.getEntityType(), 1));
	}

	@Override
	public void terminate() {

	}

}
