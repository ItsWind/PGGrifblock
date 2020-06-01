package wind.pg.PGGrifblock.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import wind.pg.PGGrifblock.PGGrifblock;

public class PGGrifblockSignChangeEvent implements Listener {
	PGGrifblock plugin;
	public PGGrifblockSignChangeEvent(PGGrifblock plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		Player ply = event.getPlayer();
		if(event.getLine(0).equalsIgnoreCase("[grifblock]")) {
			if(ply.hasPermission("pggb.admin")) {
				event.setLine(0, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("signTitle")));
				if(!plugin.arenaExists(event.getLine(1))) {
					String signMatStr = event.getBlock().getType().toString();
					Material signMat = Material.getMaterial(signMatStr.replace("WALL_", ""));
					event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(signMat, 1));
					event.getBlock().setType(Material.AIR);
					plugin.writeMessage(ply, "That's not a valid arena!");
				}
			}
		}
	}
}
