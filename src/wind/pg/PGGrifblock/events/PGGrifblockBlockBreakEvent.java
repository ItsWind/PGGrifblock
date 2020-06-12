package wind.pg.PGGrifblock.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import wind.pg.PGGrifblock.PGGrifblock;

public class PGGrifblockBlockBreakEvent implements Listener {
	PGGrifblock plugin;
	public PGGrifblockBlockBreakEvent(PGGrifblock plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onBlockBreakInArena(BlockBreakEvent event) {
		if(plugin.playerIsPlaying(event.getPlayer()) != null) {
			event.setCancelled(true);
		}
		if(plugin.isInArena(event.getBlock().getLocation()) != null) {
			if(!plugin.plyHasPerm(event.getPlayer(), "pggb.admin"))
			{
				event.setCancelled(true);
			}
		}
		else if(plugin.isArenaSign(event.getBlock())) {
			if(!plugin.plyHasPerm(event.getPlayer(), "pggb.admin"))
			{
				event.setCancelled(true);
			}
		}
	}
}