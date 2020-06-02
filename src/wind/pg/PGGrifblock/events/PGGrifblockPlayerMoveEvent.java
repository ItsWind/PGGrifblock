package wind.pg.PGGrifblock.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import wind.pg.PGGrifblock.PGGrifblock;
import wind.pg.PGGrifblock.PGGrifblockArena;
import wind.pg.PGGrifblock.PGGrifblockPlayer;

public class PGGrifblockPlayerMoveEvent implements Listener {
	PGGrifblock plugin;
	public PGGrifblockPlayerMoveEvent(PGGrifblock plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlyMove(PlayerMoveEvent event) {
		Player ply = event.getPlayer();
		if(plugin.playerIsPlaying(ply) != null) {
			PGGrifblockArena arena = plugin.playerIsPlaying(ply);
			if(!arena.grifblock.isValid())
				arena.spawnGrifblock();
			if(arena.getPlayerObj(ply).hasGrifblock()) {
				PGGrifblockPlayer plyObj = arena.getPlayerObj(ply);
				Location spawnToCheck;
				if(plyObj.getTeam().equals("RED"))
					spawnToCheck = plugin.getArenaBlockLocation(arena.arenaName, "BLUESpawn");
				else
					spawnToCheck = plugin.getArenaBlockLocation(arena.arenaName, "REDSpawn");
				if(plugin.locationNearby(spawnToCheck, ply.getLocation())) {
					plyObj.toggleGrifblock();
					ply.getWorld().createExplosion(spawnToCheck.clone().add(0, 2, 0), 16F, false, false);
					arena.teamScores.put(plyObj.getTeam(), arena.teamScores.get(plyObj.getTeam())+1);
					arena.nextRound();
					arena.updateScoreboards();
				}
			}
		}
	}
	
	@EventHandler
	public void onPlyMoveSpectate(PlayerMoveEvent event) {
		Player ply = event.getPlayer();
		if(plugin.isSpectating(ply) != null) {
			Location from = event.getFrom();
			Location to = event.getTo();
			if(plugin.isInArena(to) == null) {
				event.setTo(from);
				plugin.writeMessage(ply, "You can't exit the arena as a spectator!");
			}
		}
	}
}
