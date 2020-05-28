package wind.pg.PGGrifblock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import wind.pg.PGGrifblock.commands.PGGrifblockCommands;
import wind.pg.PGGrifblock.events.PGGrifblockPlayerClickBlockEvent;

public class PGGrifblock extends JavaPlugin {
	Map<String, PGGrifblockArena> arenas = new HashMap<String, PGGrifblockArena>();
	
	ArrayList<ItemStack> editingInventory = new ArrayList<ItemStack>();
	Map<Player, String> editingPlayers = new HashMap<Player, String>();
	Map<Player, ItemStack[]> editingPlayersInvs = new HashMap<Player, ItemStack[]>();
	
	@Override
	public void onEnable() {
		getServer().getConsoleSender().sendMessage("PGGrifblock loaded!");
		getCommand("pggb").setExecutor(new PGGrifblockCommands(this));
		
		getServer().getPluginManager().registerEvents(new PGGrifblockPlayerClickBlockEvent(this), this);
		
		Map<Material, String> editingTools = new HashMap<Material, String>();
		editingTools.put(Material.BARRIER, "Bounds");
		editingTools.put(Material.GOLD_BLOCK, "Player Spawn");
		//editingTools.put(Material.EMERALD_BLOCK, "Mob Spawns");
		//editingTools.put(Material.ENDER_CHEST, "Mystery Blocks");
		for(Material mat : editingTools.keySet()) {
			ItemStack itemToAdd = new ItemStack(mat, 1);
			ItemMeta itemToAddMeta = itemToAdd.getItemMeta();
			itemToAddMeta.setDisplayName("Set "+editingTools.get(mat));
			itemToAdd.setItemMeta(itemToAddMeta);
			editingInventory.add(itemToAdd);
		}
	}
	
	@Override
	public void onDisable() {
		getServer().getConsoleSender().sendMessage("PGGrifblock unloaded!");
	}
	
	public String isInEditMode(Player ply) {
		return editingPlayers.get(ply);
	}
	
	public void toggleEditMode(Player ply, String arenaName) {
		if(isInEditMode(ply) != null) {
			editingPlayers.remove(ply);
			ply.getInventory().setContents(editingPlayersInvs.get(ply));
			editingPlayersInvs.remove(ply);
			//removeUnclaimedData(ply);
		}
		else {
			arenas.remove(arenaName);
			editingPlayers.put(ply, arenaName);
			editingPlayersInvs.put(ply, ply.getInventory().getContents());
			//setUnclaimedData(ply, "oldInv", ply.getInventory().getContents());
			ply.getInventory().clear();
			for(ItemStack item : editingInventory) {
				ply.getInventory().addItem(item);
			}
		}
	}
	
	public File getArenaFile(String arenaName) {
		arenaName = arenaName.toLowerCase();
		File arenaFile = new File(getDataFolder().getAbsolutePath() + File.separator + "arenas" + File.separator, arenaName + ".yml");
		return arenaFile;
	}
	
	public FileConfiguration getArenaConfigFile(String arenaName) {
		File arenaFile = this.getArenaFile(arenaName);
		FileConfiguration arenaConfig = new YamlConfiguration();
		try {
			arenaConfig.load(arenaFile);
		}
		catch(IOException | InvalidConfigurationException e) {
			e.printStackTrace();
			return null;
		}
		return arenaConfig;
	}
	
	public boolean arenaExists(String arenaName) {
		File arenaFile = this.getArenaFile(arenaName);
		return arenaFile.exists();
	}
	
	public boolean createArena(String arenaName) {
		File arenasFile = this.getArenaFile(arenaName);
		if(!arenasFile.exists()) {
			try {
				arenasFile.getParentFile().mkdirs();
				arenasFile.createNewFile();
			}
			catch(IOException e) {
				return false;
			}
		}
		else {
			return false;
		}
		//FileConfiguration arenasConfig = this.getArenaConfigFile(arenaName);
		this.editArenaData(arenaName, "name", arenaName);
		//try {
		//	arenasConfig.save(arenasFile);
		//} catch (IOException e) {
		//	e.printStackTrace();
		//	return false;
		//}
		return true;
	}
	
	public void editArenaData(String arenaName, String dataToEdit, Object value) {
		File arenaFile = this.getArenaFile(arenaName);
		FileConfiguration arenaConfig = this.getArenaConfigFile(arenaName);
		arenaConfig.set(dataToEdit, value);
		try {
			arenaConfig.save(arenaFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getArenaDataString(String arenaName, String dataToGet) {
		FileConfiguration arenaConfig = this.getArenaConfigFile(arenaName);
		return arenaConfig.getString(dataToGet);
	}
	
	public int getArenaDataInt(String arenaName, String dataToGet) {
		FileConfiguration arenaConfig = this.getArenaConfigFile(arenaName);
		return arenaConfig.getInt(dataToGet);
	}
	
	public Location getArenaBlockLocation(String arenaName, String dataToGet) {
		FileConfiguration arenaConfig = this.getArenaConfigFile(arenaName);
		Location locToReturn = arenaConfig.getLocation(dataToGet);
		if(locToReturn == null)
			locToReturn = new Location(getServer().getWorld("world"), 0, 0, 0);
		return locToReturn;
	}
	
	public void removeArena(String arenaName) {
		if(this.arenaExists(arenaName)) {
			File arenaFile = this.getArenaFile(arenaName);
			arenaFile.delete();
		}
	}
	
	public ArrayList<String> allArenaNames() {
		ArrayList<String> arenaNames = new ArrayList<String>();
		for(File file : new File(getDataFolder().getAbsolutePath() + File.separator + "arenas").listFiles())
			arenaNames.add(file.getName().substring(0, file.getName().lastIndexOf(".")));
		if(arenaNames.size() > 0)
			return arenaNames;
		else
			return null;
	}
	
	public PGGrifblockArena getArenaObj(String arenaName) {
		arenaName = arenaName.toLowerCase();
		if(arenas.containsKey(arenaName)) {
			return arenas.get(arenaName);
		}
		else {
			PGGrifblockArena arena = new PGGrifblockArena(this, getArenaConfigFile(arenaName).getString("name"));
			arenas.put(arenaName, arena);
			return arenas.get(arenaName);
		}
	}
	
	public PGGrifblockArena isInArena(Location loc) {
		for(String arenaName : this.allArenaNames()) {
			if(boundCheck(arenaName, loc)) {
				return getArenaObj(arenaName);
			}
		}
		return null;
	}
	private boolean boundCheck(String arenaName, Location loc) {
		Location bounds1Loc = this.getArenaBlockLocation(arenaName, "boundingBlocks.block1");
		Location bounds2Loc = this.getArenaBlockLocation(arenaName, "boundingBlocks.block2");
		double x1 = bounds1Loc.getX();
		double y1 = bounds1Loc.getY();
		double z1 = bounds1Loc.getZ();

		double x2 = bounds2Loc.getX();
		double y2 = bounds2Loc.getY();
		double z2 = bounds2Loc.getZ();
	     
		double xP = loc.getX()+0.5;
		double yP = loc.getY()+0.5;
		double zP = loc.getZ()+0.5;

		if(((x1 <= xP && xP <= x2) || (x1 >= xP && xP >= x2)) && ((y1 <= yP && yP <= y2) || (y1 >= yP && yP >= y2)) && ((z1 <= zP && zP <= z2) || (z1 >= zP && zP >= z2))){
			return true;
		} else {
			return false;
		}
	}
	
	public void clearArenas() {
		for(String arenaName : arenas.keySet()) {
			Bukkit.getScheduler().cancelTask(getArenaObj(arenaName).queueTimer);
		}
		arenas.clear();
	}
	
	public boolean getArenasInProgress() {
		if(arenas.size() > 0) {
			for(String arenaName : arenas.keySet()) {
				if(getArenaObj(arenaName).inProgress) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void writeMessage(CommandSender ply, String message) {
		ply.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', getConfig().getString("signTitle")) + ChatColor.DARK_GRAY + "] " + ChatColor.YELLOW + message);
	}
	public void writeMessage(Player ply, String message) {
		ply.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', getConfig().getString("signTitle")) + ChatColor.DARK_GRAY + "] " + ChatColor.YELLOW + message);
	}
	
	public boolean arrayHas(Object[] array, Object value) {
		for(Object x : array) {
			if(x.equals(value)) {
				return true;
			}
		}
		return false;
	}
}
