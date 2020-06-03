package wind.pg.PGGrifblock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
import org.bukkit.util.Vector;

import wind.pg.PGGrifblock.commands.PGGrifblockCommands;
import wind.pg.PGGrifblock.commands.PGGrifblockTabCompletion;
import wind.pg.PGGrifblock.events.PGGrifblockBlockBreakEvent;
import wind.pg.PGGrifblock.events.PGGrifblockBlockPlaceEvent;
import wind.pg.PGGrifblock.events.PGGrifblockDropItemEvent;
import wind.pg.PGGrifblock.events.PGGrifblockEntityHitByEntityEvent;
import wind.pg.PGGrifblock.events.PGGrifblockInventoryClickEvent;
import wind.pg.PGGrifblock.events.PGGrifblockMobSpawnEvent;
import wind.pg.PGGrifblock.events.PGGrifblockPickupItemEvent;
import wind.pg.PGGrifblock.events.PGGrifblockPlayerClickBlockEvent;
import wind.pg.PGGrifblock.events.PGGrifblockPlayerCommandSendEvent;
import wind.pg.PGGrifblock.events.PGGrifblockPlayerDeathEvent;
import wind.pg.PGGrifblock.events.PGGrifblockPlayerFoodLevelChangeEvent;
import wind.pg.PGGrifblock.events.PGGrifblockPlayerJoinEvent;
import wind.pg.PGGrifblock.events.PGGrifblockPlayerMoveEvent;
import wind.pg.PGGrifblock.events.PGGrifblockPlayerQuitEvent;
import wind.pg.PGGrifblock.events.PGGrifblockSignChangeEvent;

public class PGGrifblock extends JavaPlugin {
	Map<String, PGGrifblockArena> arenas = new HashMap<String, PGGrifblockArena>();
	
	ArrayList<ItemStack> editingInventory = new ArrayList<ItemStack>();
	Map<Player, String> editingPlayers = new HashMap<Player, String>();
	Map<Player, ItemStack[]> editingPlayersInvs = new HashMap<Player, ItemStack[]>();
	Map<Player, String> spectatingPlayers = new HashMap<Player, String>();
	Map<Player, Location> oldSpectatingLocs = new HashMap<Player, Location>();
	
	@Override
	public void onEnable() {
		getServer().getConsoleSender().sendMessage("PGGrifblock loaded!");
		getCommand("pggb").setExecutor(new PGGrifblockCommands(this));
		getCommand("pggb").setTabCompleter(new PGGrifblockTabCompletion(this));
		
		getServer().getPluginManager().registerEvents(new PGGrifblockPlayerClickBlockEvent(this), this);
		getServer().getPluginManager().registerEvents(new PGGrifblockBlockBreakEvent(this), this);
		getServer().getPluginManager().registerEvents(new PGGrifblockMobSpawnEvent(this), this);
		getServer().getPluginManager().registerEvents(new PGGrifblockPlayerDeathEvent(this), this);
		getServer().getPluginManager().registerEvents(new PGGrifblockPickupItemEvent(this), this);
		getServer().getPluginManager().registerEvents(new PGGrifblockPlayerMoveEvent(this), this);
		getServer().getPluginManager().registerEvents(new PGGrifblockDropItemEvent(this), this);
		getServer().getPluginManager().registerEvents(new PGGrifblockEntityHitByEntityEvent(this), this);
		getServer().getPluginManager().registerEvents(new PGGrifblockPlayerCommandSendEvent(this), this);
		getServer().getPluginManager().registerEvents(new PGGrifblockPlayerJoinEvent(this), this);
		getServer().getPluginManager().registerEvents(new PGGrifblockPlayerQuitEvent(this), this);
		getServer().getPluginManager().registerEvents(new PGGrifblockSignChangeEvent(this), this);
		getServer().getPluginManager().registerEvents(new PGGrifblockPlayerFoodLevelChangeEvent(this), this);
		getServer().getPluginManager().registerEvents(new PGGrifblockInventoryClickEvent(this), this);
		getServer().getPluginManager().registerEvents(new PGGrifblockBlockPlaceEvent(this), this);
		
		this.saveDefaultConfig();
		
		Map<Material, String> editingTools = new HashMap<Material, String>();
		editingTools.put(Material.BARRIER, "Bounds");
		editingTools.put(Material.REDSTONE_BLOCK, "RED Spawn");
		editingTools.put(Material.LAPIS_BLOCK, "BLUE Spawn");
		editingTools.put(Material.GLOWSTONE, "Grifblock Spawn");
		for(Material mat : editingTools.keySet()) {
			ItemStack itemToAdd = new ItemStack(mat, 1);
			ItemMeta itemToAddMeta = itemToAdd.getItemMeta();
			itemToAddMeta.setDisplayName("Set "+editingTools.get(mat));
			itemToAdd.setItemMeta(itemToAddMeta);
			editingInventory.add(itemToAdd);
		}
		
		File arenasFolder = new File(getDataFolder().getAbsolutePath() + File.separator + "arenas");
		arenasFolder.mkdirs();
	}
	
	@Override
	public void onDisable() {
		getServer().getConsoleSender().sendMessage("PGGrifblock unloaded!");
		
		for(String arenaName : arenas.keySet()) {
			getArenaObj(arenaName).endArena();
		}
		this.clearArenas();
		this.bootAllEditMode();
		this.bootAllSpectating();
	}
	
	public void doHammerSmash(Player ply) {
		if(ply.getAttackCooldown() == 1.0) {
			PGGrifblockArena arena = this.playerIsPlaying(ply);
			Location hammerLocation = ply.getLocation().clone().add(ply.getLocation().getDirection().multiply(2));
			hammerLocation.add(0, 1.5, 0);
			//this.printToConsole(hammerLocation.toString());
			for(int i = 0; i < 8; i++)
				ply.getWorld().spawnParticle(Particle.SMOKE_LARGE, hammerLocation.clone().add(this.getRandDouble(), this.getRandDouble(), this.getRandDouble()), 1);
			ply.getWorld().playSound(hammerLocation, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 2.0F, 0.3F);
			Vector plyDirVec = ply.getLocation().subtract(hammerLocation).toVector().multiply(0.5);
			ply.setVelocity(plyDirVec.add(new Vector(0,0.25,0)));
			for(Player other : arena.getPlayers().keySet()) {
				if(!other.equals(ply)) {
					//this.printToConsole("dist " + other.getLocation().distance(hammerLocation));
					double distFromHammer = other.getLocation().add(0, 1.5, 0).distance(hammerLocation);
					if(distFromHammer <= 3.25) {
						Vector dirVec = other.getLocation().subtract(hammerLocation).toVector();
						other.setVelocity(dirVec.add(new Vector(0,0.25,0)));
						other.damage(20/(distFromHammer));
						//this.printToConsole("damage " + 15/(distFromHammer));
					}
				}
			}
		}
	}
	
	public void doMeleeSlide(Player ply, Player hit, String type) {
		if(ply.getAttackCooldown() == 1.0) {
			//PGGrifblockArena arena = this.playerIsPlaying(ply);
			Location hammerLocation = ply.getLocation().clone().add(ply.getLocation().getDirection().multiply(2));
			hammerLocation.add(0, 1.5, 0);
			
			Particle particleType = Particle.CRIT;
			Sound soundType = Sound.ENTITY_PLAYER_BIG_FALL;
			if(type.equals("Energy Sword")) {
				particleType = Particle.CRIT_MAGIC;
				soundType = Sound.ITEM_TRIDENT_RIPTIDE_1;
			}
			for(int i = 0; i < 8; i++)
				ply.getWorld().spawnParticle(particleType, hammerLocation.clone().add(this.getRandDouble(), this.getRandDouble(), this.getRandDouble()), 1);
			ply.getWorld().playSound(hammerLocation, soundType, 2.0F, 1.25F);
			
			
			Vector dirVec = hammerLocation.subtract(ply.getLocation()).toVector().multiply(0.25);
			ply.setVelocity(dirVec);
			hit.damage(20.0);
		}
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
			getArenaObj(arenaName).bootPlayer(ply);
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
	
	public String isSpectating(Player ply) {
		return spectatingPlayers.get(ply);
	}

	public void toggleSpectating(Player ply, String arenaName) {
		toggleSpectating(ply, arenaName, true);
	}
	public void toggleSpectating(Player ply, String arenaName, boolean removeBool) {
		if(isSpectating(ply) != null) {
			if(removeBool)
				spectatingPlayers.remove(ply);
			ply.setGameMode(GameMode.SURVIVAL);
			ply.teleport(oldSpectatingLocs.get(ply));
			oldSpectatingLocs.remove(ply);
			removeUnclaimedData(ply);
		}
		else {
			spectatingPlayers.put(ply, arenaName);
			oldSpectatingLocs.put(ply, ply.getLocation());
			setUnclaimedData(ply, "oldLoc", ply.getLocation());
			setUnclaimedData(ply, "wasSpectating", true);
			ply.setGameMode(GameMode.SPECTATOR);
	    	//List<Double> telCords = getArenaCordData(arenaName, "playerSpawn");
	    	//Location telLoc = new Location(getArenaWorld(arenaName), telCords.get(0), telCords.get(1)+1, telCords.get(2));
			Location telLoc = this.getArenaBlockLocation(arenaName, "grifblockSpawn");
	    	ply.teleport(telLoc);
		}
	}
	
	public void bootAllSpectating() {
		for(Player ply : spectatingPlayers.keySet()) {
			toggleSpectating(ply, null, false);
		}
		spectatingPlayers.clear();
	}
	
	public String isInEditMode(Player ply) {
		return editingPlayers.get(ply);
	}
	
	public void toggleEditMode(Player ply, String arenaName) {
		toggleEditMode(ply, arenaName, true);
	}
	public void toggleEditMode(Player ply, String arenaName, boolean removeBool) {
		if(isInEditMode(ply) != null) {
			if(removeBool)
				editingPlayers.remove(ply);
			ply.getInventory().setContents(editingPlayersInvs.get(ply));
			editingPlayersInvs.remove(ply);
			removeUnclaimedData(ply);
		}
		else {
			arenas.remove(arenaName);
			editingPlayers.put(ply, arenaName);
			editingPlayersInvs.put(ply, ply.getInventory().getContents());
			setUnclaimedData(ply, "oldInv", ply.getInventory().getContents());
			ply.getInventory().clear();
			for(ItemStack item : editingInventory) {
				ply.getInventory().addItem(item);
			}
		}
	}
	
	public void bootAllEditMode() {
		for(Player ply : editingPlayers.keySet()) {
			toggleEditMode(ply, null, false);
		}
		editingPlayers.clear();
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
		File[] fileArray = new File(getDataFolder().getAbsolutePath() + File.separator + "arenas").listFiles();
		if(fileArray.length < 1)
			return null;
		for(File file : new File(getDataFolder().getAbsolutePath() + File.separator + "arenas").listFiles()) {
			arenaNames.add(file.getName().substring(0, file.getName().lastIndexOf(".")));
		}
		if(arenaNames.size() > 0) {
			return arenaNames;
		}
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
		if(allArenaNames() != null) {
			for(String arenaName : this.allArenaNames()) {
				if(boundCheck(arenaName, loc)) {
					return getArenaObj(arenaName);
				}
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
	
	public boolean locationNearby(Location setLoc, Location toCheck) {
		Location bounds1Loc = setLoc.clone().add(1.5, 1.5, 1.5);
		Location bounds2Loc = setLoc.clone().add(-1.5, -1.5, -1.5);
		double x1 = bounds1Loc.getX();
		double y1 = bounds1Loc.getY();
		double z1 = bounds1Loc.getZ();

		double x2 = bounds2Loc.getX();
		double y2 = bounds2Loc.getY();
		double z2 = bounds2Loc.getZ();
		
		double xP = toCheck.getX();
		double yP = toCheck.getY();
		double zP = toCheck.getZ();
		
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
	
	public void printToConsole(String str) {
		getServer().getConsoleSender().sendMessage(str);
	}
}
