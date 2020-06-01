package wind.pg.PGGrifblock.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import wind.pg.PGGrifblock.PGGrifblock;

public class PGGrifblockMobSpawnEvent implements Listener {
	PGGrifblock plugin;
	public PGGrifblockMobSpawnEvent(PGGrifblock plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onMobSpawn(CreatureSpawnEvent event) {
		if(plugin.isInArena(event.getLocation()) != null) {
			event.getEntity().remove();
		}
	}
}
