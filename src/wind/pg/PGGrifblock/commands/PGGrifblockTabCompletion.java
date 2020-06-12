package wind.pg.PGGrifblock.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import wind.pg.PGGrifblock.PGGrifblock;

public class PGGrifblockTabCompletion implements TabCompleter {
	PGGrifblock plugin;
	public PGGrifblockTabCompletion(PGGrifblock plugin) {
		this.plugin = plugin;
	}
    @Override
    public List<String> onTabComplete (CommandSender sender, Command cmd, String label, String[] args){
        ArrayList<String> list = new ArrayList<String>();
        if(sender instanceof Player){
            Player ply = (Player) sender;
            if(plugin.plyHasPerm(ply, "pggb.player")) {
	        	if(cmd.getName().equalsIgnoreCase("pggb") || cmd.getName().equalsIgnoreCase("gb")) {
		        	if(args.length == 1) {
		                list.add("help");
		                list.add("join");
		                list.add("leave");
		                list.add("spectate");
		                list.add("top");
		                if(plugin.plyHasPerm(ply, "pggb.admin")) {
		                    list.add("reload");
			                list.add("create");
			                list.add("edit");
			                list.add("delete");
			                list.add("tp");
		                }
	        		}
		        	else if(args.length == 2) {
		        		String[] needsArenaName = {"join", "edit", "delete", "spectate", "tp", "top"};
		        		if(plugin.arrayHas(needsArenaName, args[0]))
		        			if(plugin.allArenaNames() != null)
		        				for(String arenaName : plugin.allArenaNames())
		        					list.add(arenaName);
		        		else if(args[0].equalsIgnoreCase("create"))
		        			list.add("{ARENANAME}");
		        	}
	            }
            }
        }
        return list;
    }
}