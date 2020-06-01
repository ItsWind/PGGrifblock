package wind.pg.PGGrifblock.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

import wind.pg.PGGrifblock.PGGrifblock;

public class PGGrifblockInventoryClickEvent implements Listener {
	PGGrifblock plugin;
	public PGGrifblockInventoryClickEvent(PGGrifblock plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlyInvClick(InventoryClickEvent event) {
		if(event.getWhoClicked() instanceof Player) {
			Player ply = (Player) event.getWhoClicked();
			if(plugin.playerIsPlaying(ply) != null) {
				if(event.getSlotType() == InventoryType.SlotType.ARMOR) {
		            event.setCancelled(true);
		        }
			}
		}
	}
}
