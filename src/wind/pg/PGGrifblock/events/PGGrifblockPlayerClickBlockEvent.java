package wind.pg.PGGrifblock.events;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;

import wind.pg.PGGrifblock.PGGrifblock;

public class PGGrifblockPlayerClickBlockEvent implements Listener {
	PGGrifblock plugin;
	public PGGrifblockPlayerClickBlockEvent(PGGrifblock plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlyClickEditMode(PlayerInteractEvent event) {
		Player ply = event.getPlayer();
		if(ply.hasPermission("pggb.admin") && plugin.isInEditMode(ply) != null && ply.getInventory().getItemInMainHand().getType() != Material.AIR) {
			event.setCancelled(true);
			String arenaName = plugin.isInEditMode(ply);
			Block blockClicked = event.getClickedBlock();
			Location blockLocation = blockClicked.getLocation();
			
			if(ply.getInventory().getItemInMainHand().getItemMeta().getDisplayName().contains("Bounds2")) {
				plugin.editArenaData(arenaName, "boundingBlocks.block2", blockLocation.add(0.5,0.5,0.5));
				ItemMeta itemMeta = ply.getInventory().getItemInMainHand().getItemMeta();
				itemMeta.setDisplayName("Set Bounds");
				ply.getInventory().getItemInMainHand().setItemMeta(itemMeta);
				plugin.writeMessage(ply, "Bounding blocks have been set!");
			}
			else if(ply.getInventory().getItemInMainHand().getItemMeta().getDisplayName().contains("Bounds")) {
				plugin.editArenaData(arenaName, "boundingBlocks.block1", blockLocation.add(0.5,0.5,0.5));
				ItemMeta itemMeta = ply.getInventory().getItemInMainHand().getItemMeta();
				itemMeta.setDisplayName("Set Bounds2");
				ply.getInventory().getItemInMainHand().setItemMeta(itemMeta);
				plugin.writeMessage(ply, "First bounding block set! Click the second bounding block.");
			}
			else if(ply.getInventory().getItemInMainHand().getItemMeta().getDisplayName().contains("Player Spawn")) {
				if(plugin.isInArena(blockLocation.add(0.5,0.5,0.5)) != null) {
					plugin.editArenaData(arenaName, "playerSpawn", blockLocation.add(0.5,0.5,0.5));
					plugin.writeMessage(ply, "Player spawn has been set!");
				}
				else {
					plugin.writeMessage(ply, "That's not within this arena's bounds!");
				}
			}
		}
	}
}
