package wind.pg.PGGrifblock.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import wind.pg.PGGrifblock.PGGrifblock;
import wind.pg.PGGrifblock.PGGrifblockArena;

public class PGGrifblockDropItemEvent implements Listener {
	PGGrifblock plugin;
	public PGGrifblockDropItemEvent(PGGrifblock plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlyDropItem(PlayerDropItemEvent event) {
		Player ply = event.getPlayer();
		if(plugin.playerIsPlaying(ply) != null) {
			if(event.getItemDrop().getItemStack().getType().equals(Material.GLOWSTONE) && event.getItemDrop().getItemStack().getItemMeta().getDisplayName().equals("Grifblock")) {
				PGGrifblockArena arena = plugin.playerIsPlaying(ply);
				arena.grifblock = event.getItemDrop();
				event.getItemDrop().setGlowing(true);
				arena.getPlayerObj(ply).toggleGrifblock();
				arena.equipStarterKit(ply);
			}
			else {
				event.setCancelled(true);
			}
		}
	}
}
