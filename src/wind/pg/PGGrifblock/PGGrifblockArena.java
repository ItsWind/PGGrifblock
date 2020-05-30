package wind.pg.PGGrifblock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import wind.pg.PGGrifblock.util.PGGrifblockRewardCommand;

public class PGGrifblockArena {
	PGGrifblock plugin;
	public boolean inProgress = false;
	public String arenaName;
	int secondsToWait;
	
	Map<Player, PGGrifblockPlayer> players = new HashMap<Player, PGGrifblockPlayer>();
	ArrayList<Block> arenaSigns = new ArrayList<Block>();
	ArrayList<ItemStack> arenaStarterKit = new ArrayList<ItemStack>();
	
	public PGGrifblockArena(PGGrifblock plugin, String arenaName) {
		this.plugin = plugin;
		this.arenaName = plugin.getArenaDataString(arenaName, "name");
		
		if(plugin.getConfig().isConfigurationSection("starterKit") && plugin.getConfig().getConfigurationSection("starterKit").getKeys(false).size() > 0) {
			ConfigurationSection starterKitSection = plugin.getConfig().getConfigurationSection("starterKit");
			for(String itemString : starterKitSection.getKeys(false)) {
				int itemAmount = 1;
				if(starterKitSection.getConfigurationSection(itemString).contains("amount"))
					itemAmount = starterKitSection.getConfigurationSection(itemString).getInt("amount");
				ItemStack itemToAdd = new ItemStack(Material.getMaterial(itemString), itemAmount);
				if(itemString.toLowerCase().contains("leather") && starterKitSection.isConfigurationSection(itemString) && starterKitSection.getConfigurationSection(itemString).isConfigurationSection("color")) {
					ConfigurationSection colorSection = starterKitSection.getConfigurationSection(itemString).getConfigurationSection("color");
					Color itemColor = Color.fromRGB(colorSection.getInt("r"), colorSection.getInt("g"), colorSection.getInt("b"));
					LeatherArmorMeta itemMeta = (LeatherArmorMeta) itemToAdd.getItemMeta();
					itemMeta.setColor(itemColor);
					itemToAdd.setItemMeta(itemMeta);
				}
				arenaStarterKit.add(itemToAdd);
			}
		}
	}
	
	int queueTimer = -1;
	public int round = 1;
	Map<Player, Location> oldLocs = new HashMap<Player, Location>();
	Map<Player, ItemStack[]> oldInvs = new HashMap<Player, ItemStack[]>();
	Map<Player, Integer> oldXp = new HashMap<Player, Integer>();
	Map<Player, Double> oldHp = new HashMap<Player, Double>();
	Map<Player, Integer> oldFood = new HashMap<Player, Integer>();
	Map<Player, GameMode> oldGm = new HashMap<Player, GameMode>();
	
	public String getName() {
		return arenaName;
	}
	
	public Map<Player, PGGrifblockPlayer> getPlayers() {
		return players;
	}
	
	public PGGrifblockPlayer getPlayerObj(Player ply) {
		return players.get(ply);
	}
	
	public void equipStarterKit(Player ply) {
		if(arenaStarterKit.size() > 0) {
			for(ItemStack itemToAdd : arenaStarterKit) {
				if(itemToAdd.getType().toString().toLowerCase().contains("chestplate")) {
					ply.getInventory().setChestplate(itemToAdd);
				}
				else if(itemToAdd.getType().toString().toLowerCase().contains("leggings")) {
					ply.getInventory().setLeggings(itemToAdd);
				}
				else if(itemToAdd.getType().toString().toLowerCase().contains("boots")) {
					ply.getInventory().setBoots(itemToAdd);
				}
				else if(itemToAdd.getType().toString().toLowerCase().contains("helmet")) {
					ply.getInventory().setHelmet(itemToAdd);
				}
				else {
					ply.getInventory().addItem(itemToAdd);
				}
			}
		}
	}
	
	public boolean checkToStart() {
		updateSigns();
		updateScoreboards();
		if(players.size() >= plugin.getArenaConfigInt(arenaName, "minPlayers")) {
			if(queueTimer == -1) {
				queueTimer = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
				    @Override
				    public void run() {
				    	if(secondsToWait % 10 == 0 || secondsToWait <= 5) {
				    		messageAllPlayers("Arena " + plugin.getArenaConfigFile(arenaName).getString("name") + " starts in " + secondsToWait + " seconds!");
				    	}
				       secondsToWait -= 1;
				       if(secondsToWait <= 0) {
				    	   startArena();
				       }
				    }
				}, 20, 20);
				return true;
			}
		}
		else {
	        Bukkit.getScheduler().cancelTask(queueTimer);
	        queueTimer = -1;
	        secondsToWait = plugin.getArenaConfigInt(arenaName, "waitTime");
		}
		return false;
	}
	
	public void checkToEnd() {
		updateSigns();
		updateScoreboards();
		if(players.size() < 1) {
			endArena();
		}
	}
	
	public void startArena() {
		inProgress = true;
        Bukkit.getScheduler().cancelTask(queueTimer);
        queueTimer = -1;
        secondsToWait = plugin.getArenaConfigInt(arenaName, "waitTime");
        messageAllPlayers("gOoOGOGo");
    	//List<Double> telCords = plugin.getArenaCordData(arenaName, "playerSpawn");
    	//Location telLoc = new Location(plugin.getArenaWorld(arenaName), telCords.get(0), telCords.get(1)+1, telCords.get(2));
        Location telLoc = plugin.getArenaBlockLocation(arenaName, "playerSpawn").add(-0.5, 0, -0.5);
        for(Player ply : players.keySet()) {
        	oldLocs.put(ply, ply.getLocation());
        	plugin.setUnclaimedData(ply, "oldLoc", ply.getLocation());
        	oldInvs.put(ply, ply.getInventory().getContents());
        	plugin.setUnclaimedData(ply, "oldInv", ply.getInventory().getContents());
        	oldXp.put(ply, ply.getLevel());
        	plugin.setUnclaimedData(ply, "oldXp", ply.getLevel());
        	oldHp.put(ply, ply.getHealth());
        	plugin.setUnclaimedData(ply, "oldHp", ply.getHealth());
        	oldFood.put(ply, ply.getFoodLevel());
        	plugin.setUnclaimedData(ply, "oldFood", ply.getFoodLevel());
        	oldGm.put(ply, ply.getGameMode());
        	plugin.setUnclaimedData(ply, "oldGm", ply.getGameMode().toString());
        	
        	ply.teleport(telLoc);
        	ply.getInventory().clear();
        	ply.setLevel(0);
        	ply.setHealth(20);
        	ply.setFoodLevel(20);
        	ply.setGameMode(GameMode.SURVIVAL);
        	
        	ply.setInvulnerable(false);
        	
        	equipStarterKit(ply);
        }
        //startWave();
	}
	
	public void endArena() {
		inProgress = false;
		//currentMaxMobNum = plugin.getArenaConfigInt(arenaName, "mobAmtStart");
		round = 1;
		updateSigns();
		//resetChests();
		//plugin.removeUnclaimedArenaContainerData(arenaName);
		oldLocs.clear();
		oldInvs.clear();
		oldXp.clear();
		
		//for(int i = mobs.size()-1; i > -1; i--) {
		//	Entity ent = mobs.get(i);
		//	ent.remove();
		//	mobs.remove(i);
		//}
		//Bukkit.getScheduler().cancelTask(waveGraceTimer);
	}
	
	public void updateSigns() {
		if(arenaSigns.size() > 0) {
			for(int i = arenaSigns.size()-1; i >= 0; i--) {
				Block signBlock = arenaSigns.get(i);
				if(signBlock.getType().toString().toLowerCase().contains("sign")) {
					String queueStr = "(" + plugin.getArenaObj(arenaName).players.size() + "/" + plugin.getArenaConfigInt(arenaName, "maxPlayers") + ")";
					plugin.editSign(signBlock, 2, queueStr);
					plugin.editSign(signBlock, 3, "Round " + round);
				}
				else {
					arenaSigns.remove(signBlock);
				}
			}
		}
	}
	
	public void updateScoreboards() {
		if(players.size() > 0) {
			for(Player ply : players.keySet()) {
				PGGrifblockPlayer arenaPlayer = players.get(ply);
				arenaPlayer.updateScoreboard();
			}
		}
	}
	
	public void addSign(Block signBlock) {
		if(!arenaSigns.contains(signBlock)) {
			arenaSigns.add(signBlock);
		}
	}
	
	public void removeSign(Block signBlock) {
		if(arenaSigns.contains(signBlock)) {
			arenaSigns.remove(signBlock);
		}
	}
	
	public void messageAllPlayers(String message) {
		for(Player ply : players.keySet()) {
			plugin.writeMessage(ply, message);
		}
	}
	
	public void bootPlayer(Player ply, boolean died) {
		bootPlayer(ply, died, true);
	}
	public void bootPlayer(Player ply, boolean died, boolean removeBool) {
		//addTopScore(ply, wave);
		//getPlayer(ply).clearPerks();
		if(removeBool)
			players.remove(ply);
		updateScoreboards();
		ply.setInvulnerable(false);
		plugin.removeUnclaimedData(ply);
		
		ply.teleport(oldLocs.get(ply));
		oldLocs.remove(ply);
		
		ply.getInventory().setContents(oldInvs.get(ply));
		oldInvs.remove(ply);
		
		ply.setLevel(oldXp.get(ply));
		oldXp.remove(ply);
		
		ply.setHealth(oldHp.get(ply));
		oldHp.remove(ply);
		
		ply.setFoodLevel(oldFood.get(ply));
		oldFood.remove(ply);
		
		ply.setGameMode(oldGm.get(ply));
		oldGm.remove(ply);
		
		ply.setHealth(20.0);
		ply.setFireTicks(0);
		
		String notifyBootMessage = " has died!";
		String[] notifyBootMessages = {" has left!", " has died!"};
		notifyBootMessage = notifyBootMessages[plugin.boolToInt(died)];
		messageAllPlayers(ply.getName() + notifyBootMessage);
		
		int randMeanieNum = plugin.getRandNum(1, 1000);
		String plyBootMessage = "You died!";
		String[] bootMessages = {"You left!", "You died!", "You fucking imbecile. Why were you even born? Honestly, I can't even imagine being you and wanting to be alive."};
		plyBootMessage = bootMessages[plugin.boolToInt(died)];
		if(randMeanieNum == 27)
			plyBootMessage = bootMessages[2];
		plugin.writeMessage(ply, plyBootMessage);
		ply.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		
		if(plugin.getConfig().getList("arenaRewardCommands").size() > 0) {
			for(Object obj : plugin.getConfig().getList("arenaRewardCommands")) {
				if(obj instanceof String) {
					String str = (String) obj;
					PGGrifblockRewardCommand rewardCommand = new PGGrifblockRewardCommand(plugin, str, ply, round);
					if(rewardCommand.notZero())
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), rewardCommand.getNewCommandString());
				}
			}
		}
		checkToEnd();
	}
}
