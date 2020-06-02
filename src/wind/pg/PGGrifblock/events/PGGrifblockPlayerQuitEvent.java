package wind.pg.PGGrifblock.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import wind.pg.PGGrifblock.PGGrifblock;

public class PGGrifblockPlayerQuitEvent implements Listener {
	PGGrifblock plugin;
	public PGGrifblockPlayerQuitEvent(PGGrifblock plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlyQuit(PlayerQuitEvent event) {
		Player ply = event.getPlayer();
		if(plugin.playerIsQueued(ply) != null) {
			plugin.removePlayerFromArenaQueue(plugin.playerIsQueued(ply).arenaName, ply, false);
		}
		else if(plugin.playerIsPlaying(ply) != null) {
			plugin.removePlayerFromArenaQueue(plugin.playerIsPlaying(ply).arenaName, ply, false);
		}
		
		if(plugin.isInEditMode(ply) != null) {
			plugin.toggleEditMode(ply, null);
		}
		
		if(plugin.isSpectating(ply) != null) {
			plugin.toggleSpectating(ply, null);
		}
	}
}
