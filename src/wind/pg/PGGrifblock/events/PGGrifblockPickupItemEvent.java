package wind.pg.PGGrifblock.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

import wind.pg.PGGrifblock.PGGrifblock;

public class PGGrifblockPickupItemEvent implements Listener {
	PGGrifblock plugin;
	public PGGrifblockPickupItemEvent(PGGrifblock plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onEntityPickUpItem(EntityPickupItemEvent event) {
		if(event.getEntity() instanceof Player) {
			Player ply = (Player) event.getEntity();
			if(plugin.playerIsPlaying(ply) != null) {
				if(event.getItem().getItemStack().getItemMeta().getDisplayName().equals("Grifblock")) {
					plugin.playerIsPlaying(ply).getPlayerObj(ply).toggleGrifblock();
				}
			}
		}
	}
}
