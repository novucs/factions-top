package net.novucs.ftop.logger;

import net.novucs.ftop.FactionsTopPlugin;
import net.novucs.ftop.PluginService;
import net.novucs.ftop.entity.FactionWorth;
import net.novucs.ftop.util.TreeIterator;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FactionsTopLogger implements PluginService {

	private static final SimpleDateFormat prettyFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' h:mm a");
	private static final SimpleDateFormat filenameFormat = new SimpleDateFormat("yyyy-MM-dd'_'hhmm'_'a");

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private final FactionsTopPlugin plugin;

	public FactionsTopLogger(FactionsTopPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void initialize() {
		long mills = LocalDateTime.now().getHour() < 15 ?
				LocalDateTime.now().until(LocalDate.now().atStartOfDay().plusHours(15), ChronoUnit.MILLIS) :
				LocalDateTime.now().until(LocalDate.now().plusDays(1).atStartOfDay().plusHours(15), ChronoUnit.MILLIS);

		scheduler.scheduleAtFixedRate(() -> Bukkit.getScheduler().runTask(plugin, () -> {
			File folder = Paths.get(plugin.getDataFolder().getAbsolutePath(), "logs").toFile();
			File file = new File(folder, filenameFormat.format(new Date()) + ".txt");

			List<String> strings = new ArrayList<>();
			strings.add(prettyFormat.format(new Date()));
			strings.add(" ");

			TreeIterator<FactionWorth> it;

			try {
				it = plugin.getWorthManager().getOrderedFactions().iterator();

				if (plugin.getWorthManager().getOrderedFactions().isEmpty())
					return;
			} catch (Exception e) {
				return;
			}

			for (int i = 0; i < 10; i++) {
				if (!it.hasNext())
					break;

				FactionWorth worth = it.next();
				strings.add("#" + (i+1) + ": " + worth.getName() + " " +
						NumberFormat.getCurrencyInstance(Locale.US).format(worth.getTotalWorth()));
			}

			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				try {
					folder.mkdir();
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}

				try(PrintWriter out = new PrintWriter(file)){
					strings.forEach(out::println);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			});
		}), mills, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);
	}

	@Override
	public void terminate() {
		scheduler.shutdownNow();
	}

}
