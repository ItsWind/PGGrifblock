package wind.pg.PGGrifblock.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import wind.pg.PGGrifblock.PGGrifblock;
import wind.pg.PGGrifblock.PGGrifblockArena;

public class PGGrifblockPlayerTeleportEvent implements Listener {
	PGGrifblock plugin;
	public PGGrifblockPlayerTeleportEvent(PGGrifblock plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlyTeleportInArena(PlayerTeleportEvent event) {
		Player ply = event.getPlayer();
		Location telLoc = event.getTo();
		PGGrifblockArena arena = plugin.isInArena(telLoc);
		if(plugin.isSpectating(ply) == null && arena != null && arena.inProgress && plugin.playerIsPlaying(ply) == null && !plugin.plyHasPerm(ply, "pggb.bypass")) {
			event.setCancelled(true);
			ply.teleport(event.getFrom());
			plugin.writeMessage(ply, "You cannot teleport to a game in progress!");
		}
	}
}
