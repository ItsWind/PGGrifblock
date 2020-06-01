package wind.pg.PGGrifblock.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import wind.pg.PGGrifblock.PGGrifblock;

public class PGGrifblockPlayerDeathEvent implements Listener {
	PGGrifblock plugin;
	public PGGrifblockPlayerDeathEvent(PGGrifblock plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlyArenaDeath(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player && plugin.playerIsPlaying((Player)event.getEntity()) != null) {//plugin.playingPlayers.containsKey((Player) event.getEntity())) {
			Player ply = (Player) event.getEntity();
			if(ply.getHealth() - event.getDamage() < 0.5) {
				event.setCancelled(true);
				plugin.playerIsPlaying(ply).getPlayerObj(ply).resetInArena();
			}
		}
	}
	
	@EventHandler
	public void onPlyDeath(PlayerDeathEvent event) {
		Player ply = event.getEntity();
		if(plugin.playerIsQueued(ply) != null) {
			plugin.removePlayerFromArenaQueue(plugin.playerIsQueued(ply).arenaName, ply, true);
			plugin.writeMessage(ply, "You have been removed from queue due to dying!");
		}
	}
}
