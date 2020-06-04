package wind.pg.PGGrifblock.commands;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import wind.pg.PGGrifblock.PGGrifblock;

public class PGGrifblockCommands implements CommandExecutor {
	PGGrifblock plugin;
	public PGGrifblockCommands(PGGrifblock plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender.hasPermission("pggb.player")) {
			if(args.length > 0 && args[0].equalsIgnoreCase("add")) {
				plugin.addPlayerToArenaQueue("grifword", Bukkit.getPlayer(args[1]));
			}
			if(args.length < 1 || args[0].equalsIgnoreCase("help")) {
				String helpString = "";
				Map<String, String> commandStrings = new HashMap<String, String>();
				commandStrings.put("help", "Displays this help page.");
				commandStrings.put("join", "Queues you for an arena.");
				commandStrings.put("leave", "Disconnects you from any current arena.");
				commandStrings.put("spectate", "Toggles spectating of an arena.");
				//commandStrings.put("top", "Shows top five scores for the given arena.");
				commandStrings.put("admin|tp", "Teleport to the given arena.");
				commandStrings.put("admin|create", "Creates an arena with the given name.");
				commandStrings.put("admin|edit", "Toggles edit mode for the arena you're currently standing in or with the given name.");
				commandStrings.put("admin|delete", "Deletes an arena with the given name.");
				commandStrings.put("admin|reload", "Reloads the config and resets arenas if they are empty.");
				helpString += ChatColor.YELLOW + "--------------------" + ChatColor.WHITE + " Help " + ChatColor.YELLOW + "--------------------\n";
				for(String cmdStr : commandStrings.keySet()) {
					if(!cmdStr.contains("admin") || (cmdStr.contains("admin|") && sender.hasPermission("pggb.admin"))) {
						String newCmd = cmdStr;
						newCmd = newCmd.replace("admin|", "");
						helpString += ChatColor.DARK_GRAY + "? " + ChatColor.GOLD + "/pggb " + newCmd + ChatColor.WHITE + ": " + commandStrings.get(cmdStr) + "\n";
					}
				}
				sender.sendMessage(helpString);
			}
			else if(args[0].equalsIgnoreCase("leave")) {
				if(sender instanceof Player) {
					Player ply = (Player) sender;
					if(plugin.playerIsPlaying(ply) != null) {
						plugin.removePlayerFromArenaQueue(plugin.playerIsPlaying(ply).arenaName, ply, false);
					}
					else if(plugin.playerIsQueued(ply) != null) {
						plugin.removePlayerFromArenaQueue(plugin.playerIsQueued(ply).arenaName, ply, false);
					}
				}
			}
			else if(args[0].equalsIgnoreCase("join")) {
				if(sender instanceof Player) {
					Player ply = (Player) sender;
					if(plugin.playerIsPlaying(ply) == null && plugin.playerIsQueued(ply) == null) {
						if(args.length > 1 && plugin.arenaExists(args[1])) {
							String arenaName = args[1];
							plugin.addPlayerToArenaQueue(arenaName, ply);
						}
						else {
							plugin.writeMessage(ply, "No arena found.");
						}
					}
				}
			}
			else if(args[0].equalsIgnoreCase("spectate")) {
				if(sender instanceof Player) {
					Player ply = (Player) sender;
					if(plugin.playerIsPlaying(ply) == null && plugin.playerIsQueued(ply) == null && plugin.isInEditMode(ply) == null) {
						if(plugin.isSpectating(ply) != null) {
							plugin.toggleSpectating(ply, null);
							plugin.writeMessage(ply, "You have stopped spectating!");
							return false;
						}
						if(args.length > 1 && plugin.arenaExists(args[1])) {
							String arenaName = args[1];
							plugin.writeMessage(ply, "You are now spectating " + arenaName + "! Use /pggb spectate whenever you want to exit!");
							plugin.toggleSpectating(ply, arenaName);
						}
						else {
							plugin.writeMessage(ply, "No arena found.");
						}
					}
				}
			}
			else if(args[0].equalsIgnoreCase("reload")) {
				if(!sender.hasPermission("pggb.admin")) {
					plugin.writeMessage(sender, "You have to have the pggb.admin permission to use that!");
					return false;
				}
				if(!plugin.getArenasInProgress()) {
					plugin.reloadConfig();
					plugin.clearArenas();
					plugin.writeMessage(sender, "PGGrifblock config reloaded!");
				}
				else {
					plugin.writeMessage(sender, "There are still arenas in progress!");
				}
			}
			else if(args[0].equalsIgnoreCase("create")) {
				if(!sender.hasPermission("pggb.admin")) {
					plugin.writeMessage(sender, "You have to have the pggb.admin permission to use that!");
					return false;
				}
				if(args.length > 1) {
					String[] blacklistNames = {"exit"};
					if(plugin.arrayHas(blacklistNames, args[1])) {
						plugin.writeMessage(sender, "Arenas cannot be named " + args[1] + "!");
						return false;
					}
					if(plugin.createArena(args[1])) {
						plugin.writeMessage(sender, args[1] + " has been created!");
					}
					else {
						plugin.writeMessage(sender, "Something went wrong! Maybe " + args[1] + " is already made?");
					}
				}
				else {
					plugin.writeMessage(sender, "You have to specify a name for the arena!");
				}
			}
			else if(args[0].equalsIgnoreCase("tp")) {
				if(!sender.hasPermission("pggb.admin")) {
					plugin.writeMessage(sender, "You have to have the pggb.admin permission to use that!");
					return false;
				}
				if(sender instanceof Player) {
					Player ply = (Player) sender;
					if(args.length > 1 && plugin.arenaExists(args[1])) {
						ply.teleport(plugin.getArenaBlockLocation(args[1], "grifblockSpawn"));
						//Location spawnLoc = new Location(plugin.getArenaObj(args[1]).arenaWorld, plugin.getArenaCordData(args[1], "playerSpawn").get(0), plugin.getArenaCordData(args[1], "playerSpawn").get(1)+1, plugin.getArenaCordData(args[1], "playerSpawn").get(2));
						//ply.teleport(spawnLoc);
					}
					else {
						plugin.writeMessage(ply, "That's not a valid arena!");
					}
				}
			}
			else if(args[0].equalsIgnoreCase("edit")) {
				if(!sender.hasPermission("pggb.admin")) {
					plugin.writeMessage(sender, "You have to have the pggb.admin permission to use that!");
					return false;
				}
				if(sender instanceof Player) {
					Player ply = (Player) sender;
					//if(plugin.playerIsPlaying(ply) == null && plugin.playerIsQueued(ply) == null && plugin.isSpectating(ply) == null) {
						/*if(plugin.isInEditMode(ply) != null) {
							plugin.toggleEditMode(ply);
							plugin.writeMessage(ply, "Edit mode has been ended!");
							return false;
						}*/
					if(plugin.isInEditMode(ply) != null) {
						plugin.writeMessage(ply, "Edit mode has been ended!");
						plugin.toggleEditMode(ply, null);
						return true;
					}
					String arenaName = null;
					if(plugin.isInArena(ply.getLocation()) != null) {
						arenaName = plugin.isInArena(ply.getLocation()).arenaName;
					}
					else {
						if(args.length > 1 && plugin.arenaExists(args[1])) {
							arenaName = args[1];
						}
						else {
							plugin.writeMessage(ply, "No arena found.");
						}
					}
					if(arenaName != null && !plugin.getArenaObj(arenaName).inProgress) {
						plugin.writeMessage(ply, "You are now editing " + arenaName + "! Use /pggb edit whenever you want to exit edit mode!");
						plugin.toggleEditMode(ply, arenaName);
					}
				}
				//else {
				//	plugin.writeMessage(ply, "You can't toggle edit mode while playing or queued!");
				//}
			}
			else if(args[0].equalsIgnoreCase("delete")) {
				if(!sender.hasPermission("pggb.admin")) {
					plugin.writeMessage(sender, "You have to have the pggb.admin permission to use that!");
					return false;
				}
				if(args.length > 1) {
					File arenasFolder = new File(plugin.getDataFolder() + File.separator + "arenas" + File.separator);
					File[] arenaFiles = arenasFolder.listFiles();
					for(File file : arenaFiles) {
					    if(file.isFile()) {
					    	if(file.getName().substring(0, file.getName().lastIndexOf('.')).equalsIgnoreCase(args[1])) {
					    		if(file.delete()) {
					    			plugin.writeMessage(sender, args[1] + " has been deleted!");
					    			return true;
					    		}
					    		else {
					    			plugin.writeMessage(sender, args[1] + " failed to delete for some reason. :(");
					    			return false;
					    		}
					    	}
					    }
					}
					plugin.writeMessage(sender, "No arena has that name! Try again.");
				}
				else {
					plugin.writeMessage(sender, "You have to specify a name for the arena!");
				}
			}
			else {
				plugin.writeMessage(sender, "That's not a valid command! Try \"/pggb help\" instead!");
				//plugin.writeMessage(sender, "How did you even get here? Are you some kind of wizard or some shit? Did you type in some fancy characters or whatever the fuck you kids do these days? Do you know how hard it fucking is to make a plugin you trash? I'm sure that you don't, because you wouldn't be sitting here trying to fucking break mine. Stop your shit.");
			}
		}
		return false;
	}
}
