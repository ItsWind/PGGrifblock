package wind.pg.PGGrifblock.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import wind.pg.PGGrifblock.PGGrifblock;

public class PGGrifblockPlayerFoodLevelChangeEvent implements Listener {
	PGGrifblock plugin;
	public PGGrifblockPlayerFoodLevelChangeEvent(PGGrifblock plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlyFoodDecrease(FoodLevelChangeEvent event) {
		if(plugin.playerIsPlaying((Player) event.getEntity()) != null) {//if(plugin.playingPlayers.containsKey(event.getEntity())) {
			event.setCancelled(true);
		}
	}
}
