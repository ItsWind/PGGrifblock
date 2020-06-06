package wind.pg.PGGrifblock.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import wind.pg.PGGrifblock.PGGrifblock;

public class PGGrifblockPlayerCommandSendEvent implements Listener {
	PGGrifblock plugin;
	public PGGrifblockPlayerCommandSendEvent(PGGrifblock plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlySendCommand(PlayerCommandPreprocessEvent event) {
		Player ply = event.getPlayer();
		if(plugin.playerIsPlaying(ply) != null || plugin.isSpectating(ply) != null) {
			String[] commandArgs = event.getMessage().split(" ");
			if((!commandArgs[0].equalsIgnoreCase("/pggb")) && (!commandArgs[0].equalsIgnoreCase("/gb"))) {
				if(!ply.hasPermission("pggb.bypass")) {
					event.setCancelled(true);
					plugin.writeMessage(ply, "You cannot use commands while playing or spectating!");
				}
			}
		}
	}
}
