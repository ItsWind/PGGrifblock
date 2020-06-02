package wind.pg.PGGrifblock.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import wind.pg.PGGrifblock.PGGrifblock;

public class PGGrifblockBlockPlaceEvent implements Listener {
	PGGrifblock plugin;
	public PGGrifblockBlockPlaceEvent(PGGrifblock plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if(!event.getPlayer().hasPermission("pggb.admin") && plugin.isInArena(event.getBlock().getLocation()) != null) {
			event.setCancelled(true);
		}
	}
}
