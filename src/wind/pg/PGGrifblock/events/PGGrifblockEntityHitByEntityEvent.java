package wind.pg.PGGrifblock.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import wind.pg.PGGrifblock.PGGrifblock;

public class PGGrifblockEntityHitByEntityEvent implements Listener {
	PGGrifblock plugin;
	public PGGrifblockEntityHitByEntityEvent(PGGrifblock plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlyHitPly(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player && plugin.playerIsPlaying((Player) event.getEntity()) != null) {
			if(event.getDamager() instanceof Player && plugin.playerIsPlaying((Player) event.getDamager()) != null) {
				Player ply = (Player) event.getDamager();
				Player hit = (Player) event.getEntity();
				if(!ply.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
					event.setDamage(0);
					event.setCancelled(true);
					String itemName = ply.getInventory().getItemInMainHand().getItemMeta().getDisplayName();
					if(itemName.equals("Gravity Hammer")) {
						plugin.doHammerSmash(ply);
					}
					else if(itemName.equals("Energy Sword") || itemName.equals("Grifblock")) {
						plugin.doMeleeSlide(ply, hit, itemName);
					}
				}
				else {
					event.setCancelled(true);
				}
			}
		}
	}
}
