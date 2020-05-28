package wind.pg.PGGrifblock.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import wind.pg.PGGrifblock.PGGrifblock;

public class PGGrifblockCommands implements CommandExecutor {
	PGGrifblock plugin;
	public PGGrifblockCommands(PGGrifblock plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender.hasPermission("pgprotect.admin") && sender instanceof Player) {
			
		}
		return false;
	}
	
}
