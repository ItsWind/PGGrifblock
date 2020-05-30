package wind.pg.PGGrifblock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
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
		
		this.saveDefaultConfig();
		
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
	
	public void createUnclaimedData(Player ply) {
		File unclaimedDataFile = new File(getDataFolder() + File.separator + "unclaimeddata" + File.separator, ply.getUniqueId().toString() + ".yml");
		if(!unclaimedDataFile.exists()) {
			try {
				unclaimedDataFile.getParentFile().mkdirs();
				unclaimedDataFile.createNewFile();
			}
			catch(IOException e) {
				return;
			}
		}
	}
	
	public boolean hasUnclaimedData(Player ply) {
		File unclaimedDataFile = new File(getDataFolder() + File.separator + "unclaimeddata" + File.separator, ply.getUniqueId().toString() + ".yml");
		if(unclaimedDataFile.exists()) {
			return true;
		}
		return false;
	}
	
	public FileConfiguration getUnclaimedData(Player ply) {
		File unclaimedDataFile = new File(getDataFolder() + File.separator + "unclaimeddata" + File.separator, ply.getUniqueId().toString() + ".yml");
		createUnclaimedData(ply);
		FileConfiguration unclaimedDataConfig = new YamlConfiguration();
		try {
			unclaimedDataConfig.load(unclaimedDataFile);
		}
		catch(IOException | InvalidConfigurationException e) {
			e.printStackTrace();
			return null;
		}
		return unclaimedDataConfig;
	}
	
	public void setUnclaimedData(Player ply, String dataName, Object value) {
		File unclaimedDataFile = new File(getDataFolder() + File.separator + "unclaimeddata" + File.separator, ply.getUniqueId().toString() + ".yml");
		createUnclaimedData(ply);
		FileConfiguration unclaimedDataConfig = new YamlConfiguration();
		try {
			unclaimedDataConfig.load(unclaimedDataFile);
		}
		catch(IOException | InvalidConfigurationException e) {
			e.printStackTrace();
			return;
		}
		
		unclaimedDataConfig.set(dataName, value);
		
		try {
			unclaimedDataConfig.save(unclaimedDataFile);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void removeUnclaimedData(Player ply) {
		File unclaimedDataFile = new File(getDataFolder() + File.separator + "unclaimeddata" + File.separator, ply.getUniqueId().toString() + ".yml");
		unclaimedDataFile.delete();
	}
	
	public FileConfiguration getUnclaimedArenaContainerConfig(String arenaName) {
		arenaName = arenaName.toLowerCase();
		File unclaimedDataFile = new File(getDataFolder() + File.separator + "unclaimeddata" + File.separator, arenaName + ".yml");
		if(!unclaimedDataFile.exists()) {
			try {
				unclaimedDataFile.getParentFile().mkdirs();
				unclaimedDataFile.createNewFile();
			}
			catch(IOException e) {
				return null;
			}
		}
		FileConfiguration unclaimedDataConfig = new YamlConfiguration();
		try {
			unclaimedDataConfig.load(unclaimedDataFile);
		}
		catch(IOException | InvalidConfigurationException e) {
			e.printStackTrace();
			return null;
		}
		return unclaimedDataConfig;
	}
	
	public boolean addPlayerToArenaQueue(String arenaName, Player ply) {
		arenaName = arenaName.toLowerCase();
		//if(isSpectating(ply) == null && getEditMode(ply) == null) {
			if(!getArenaObj(arenaName).inProgress) {
				if(getArenaObj(arenaName).getPlayers().containsKey(ply)) {
					writeMessage(ply, "You are already queued in this arena!");
					getArenaObj(arenaName).updateSigns();
					return false;
				}
				else if(playerIsPlaying(ply) != null || playerIsQueued(ply) != null) {
					writeMessage(ply, "You are already in another arena!");
					getArenaObj(arenaName).updateSigns();
					return false;
				}
				else if(getArenaObj(arenaName).players.size() >= getArenaConfigInt(arenaName, "maxPlayers")) {
					writeMessage(ply, "This arena queue is full!");
					getArenaObj(arenaName).updateSigns();
					return false;
				}
				PGGrifblockArena arena = getArenaObj(arenaName);
				arena.messageAllPlayers(ply.getName() + " has entered the queue for " + getArenaConfigFile(arenaName).getString("name") + "!\n(" + (arena.getPlayers().size()+1) + "/" + getArenaConfigInt(arenaName, "maxPlayers") + ")");
				arena.players.put(ply, new PGGrifblockPlayer(this, arena, ply));
				writeMessage(ply, "You have joined the queue for " + getArenaConfigFile(arenaName).getString("name") + "!\n(" + arena.getPlayers().size() + "/" + getArenaConfigInt(arenaName, "maxPlayers") + ")");
				getArenaObj(arenaName).checkToStart();
				return true;
			}
			else {
				writeMessage(ply, getArenaConfigFile(arenaName).getString("name") + " is already in progress!");
				getArenaObj(arenaName).updateSigns();
				return false;
			}
	}
	
	public boolean removePlayerFromArenaQueue(String arenaName, Player ply, boolean died) {
		arenaName = arenaName.toLowerCase();
		ply.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		if(!getArenaObj(arenaName).inProgress) {
			if(!getArenaObj(arenaName).getPlayers().containsKey(ply)) {
				writeMessage(ply, "You aren't queued in this arena!");
				getArenaObj(arenaName).updateSigns();
				return false;
			}
			PGGrifblockArena arena = getArenaObj(arenaName);
			arena.players.remove(ply);
			arena.messageAllPlayers(ply.getName() + " has left the queue for " + getArenaConfigFile(arenaName).getString("name") + "!\n(" + arena.getPlayers().size() + "/" + getArenaConfigInt(arenaName, "maxPlayers") + ")");
			writeMessage(ply, "You have left the queue for " + getArenaConfigFile(arenaName).getString("name") + "!");
			arena.checkToStart();
			arena.checkToEnd();
		}
		else {
			getArenaObj(arenaName).bootPlayer(ply, died);
		}
		return true;
	}
	
	public double getArenaConfigDouble(String arenaName, String configKey) {
		FileConfiguration arenaConfig = getArenaConfigFile(arenaName);
		if(arenaConfig.getKeys(false).contains(configKey)) {
			return arenaConfig.getDouble(configKey);
		}
		else {
			return getConfig().getDouble(configKey);
		}
	}
	public int getArenaConfigInt(String arenaName, String configKey) {
		FileConfiguration arenaConfig = getArenaConfigFile(arenaName);
		if(arenaConfig.getKeys(false).contains(configKey)) {
			return arenaConfig.getInt(configKey);
		}
		else {
			return getConfig().getInt(configKey);
		}
	}
	
	public boolean isArenaSign(Block signBlock) {
		if(signBlock.getType().toString().toLowerCase().contains("sign")) {
			Sign sign = (Sign) signBlock.getState();
			if(sign.getLine(0).equals(ChatColor.translateAlternateColorCodes('&', getConfig().getString("signTitle")))) {
				return true;
			}
		}
		return false;
	}
	
	public void editSign(Block signBlock, int line, String message) {
		if(signBlock.getType().toString().toLowerCase().contains("sign")) {
			Sign sign = (Sign) signBlock.getState();
			sign.setLine(line, message);
			sign.update();
		}
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
	
	public PGGrifblockArena playerIsQueued(Player ply) {
		for(String arenaName : arenas.keySet()) {
			if(getArenaObj(arenaName).getPlayers().containsKey(ply) && !getArenaObj(arenaName).inProgress) {
				return getArenaObj(arenaName);
			}
		}
		return null;
	}
	
	public PGGrifblockArena playerIsPlaying(Player ply) {
		for(String arenaName : arenas.keySet()) {
			if(getArenaObj(arenaName).getPlayers().containsKey(ply) && getArenaObj(arenaName).inProgress) {
				return getArenaObj(arenaName);
			}
		}
		return null;
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
	
	public double getRandDouble() {
		Random r = new Random();
		r.setSeed(r.nextInt((int) System.currentTimeMillis()));
		int negativeOrPositive = r.nextInt(2);
		if(negativeOrPositive == 1)
			return r.nextDouble();
		else
			return -(r.nextDouble());
	}
	
	public int getRandNum(int min, int max) {

		if (min > max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		Random r = new Random();
		r.setSeed(r.nextInt((int) System.currentTimeMillis()));
		return r.nextInt((max - min) + 1) + min;
	}
	
	public int boolToInt(boolean bool) {
		if(bool)
			return 1;
		return 0;
	}
	
	public boolean isNumeric(String strNum) {
	    if (strNum == null) {
	        return false;
	    }
	    try {
	        Double.parseDouble(strNum);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}
}
