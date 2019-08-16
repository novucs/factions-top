package net.novucs.ftop.delayedspawners;

import com.google.common.collect.ImmutableMap;
import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.PluginService;
import net.novucs.ftop.RecalculateReason;
import net.novucs.ftop.WorthType;
import net.novucs.ftop.entity.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DelayedSpawners implements PluginService {

	private static final long DELAY = TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS);

	private final Map<DelayedSpawner, Long> activationMap = Collections.synchronizedMap(new LinkedHashMap<>());
	private final FactionsTopPlugin plugin;

	public DelayedSpawners(FactionsTopPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void initialize() {
		load();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::validateEntries, 20, 20);
		Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::save, 20 * 5, 20 * 5);
	}

	public void queue(CreatureSpawner spawner) {
		activationMap.put(DelayedSpawner.of(spawner), System.currentTimeMillis() + DELAY);
	}

	public void queue(DelayedSpawner spawner, long time) {
		activationMap.put(spawner, time);
	}

	public void removeFromQueue(CreatureSpawner spawner) {
		activationMap.remove(DelayedSpawner.of(spawner));
	}

	public boolean isDelayed(CreatureSpawner spawner) {
		return Optional.ofNullable(activationMap.get(DelayedSpawner.of(spawner)))
				.map(done -> System.currentTimeMillis() < done).orElse(false);
	}

	private void validateEntries() {
		Iterator<Map.Entry<DelayedSpawner, Long>> it = activationMap.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<DelayedSpawner, Long> entry = it.next();

			if (entry.getValue() > System.currentTimeMillis())
				break;

			validate(entry.getKey());
			it.remove();
		}
	}

	private void validate(DelayedSpawner spawner) {
		Chunk chunk = spawner.getPos().getBlock(plugin.getServer()).getChunk();
		double price = plugin.getSettings().getSpawnerPrice(spawner.getEntityType());

		plugin.getWorthManager().add(chunk, RecalculateReason.DELAYED_SPAWNER, WorthType.SPAWNER, price,
				ImmutableMap.of(), ImmutableMap.of(spawner.getEntityType(), 1));
	}

	@Override
	public void terminate() {
		save();
	}

	private void load() {
		File file = new File(plugin.getDataFolder(), "delayedspawners.yml");

		if (!file.exists())
			return;

		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

		if (!config.contains("list"))
			return;

		List<String> strings = new ArrayList<>(config.getStringList("list"));

		strings.forEach(string -> {
			try {
				String[] split = string.split(",");
				long expire = Long.parseLong(split[0]);
				int x = Integer.parseInt(split[1]), y = Integer.parseInt(split[2]), z = Integer.parseInt(split[3]);
				String world = split[4];
				EntityType type = EntityType.valueOf(split[5]);

				DelayedSpawner spawner = new DelayedSpawner(BlockPos.of(world, x, y, z), type);
				queue(spawner, expire);
			} catch (Exception e) {
				plugin.getLogger().log(Level.WARNING, "Error parsing delayed spawner: " + string);
				e.printStackTrace();
			}
		});
	}

	private void save() {
		File file = new File(plugin.getDataFolder(), "delayedspawners.yml");

		List<String> strings = activationMap.entrySet().stream().map(entry -> {
			DelayedSpawner spawner = entry.getKey();
			BlockPos pos = spawner.getPos();
			return Stream.of(
					entry.getValue(), pos.getX(), pos.getY(), pos.getZ(),
					pos.getWorld(), spawner.getEntityType().name()
			).map(String::valueOf).collect(Collectors.joining(","));
		}).collect(Collectors.toList());

		YamlConfiguration config = new YamlConfiguration();
		config.set("list", strings);

		try {
			config.save(file);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
