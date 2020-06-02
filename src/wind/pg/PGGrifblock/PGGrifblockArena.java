package wind.pg.PGGrifblock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import wind.pg.PGGrifblock.util.PGGrifblockRewardCommand;

public class PGGrifblockArena {
	PGGrifblock plugin;
	public boolean inProgress = false;
	public String arenaName;
	int secondsToWait;
	
	public Entity grifblock;
	
	Map<Player, PGGrifblockPlayer> players = new HashMap<Player, PGGrifblockPlayer>();
	Map<Player, PGGrifblockPlayer> redTeam = new HashMap<Player, PGGrifblockPlayer>();
	Map<Player, PGGrifblockPlayer> blueTeam = new HashMap<Player, PGGrifblockPlayer>();
	public Map<String, Integer> teamScores = new HashMap<String, Integer>();
	
	ArrayList<Block> arenaSigns = new ArrayList<Block>();
	ArrayList<ItemStack> arenaStarterKit = new ArrayList<ItemStack>();
	
	public PGGrifblockArena(PGGrifblock plugin, String arenaName) {
		this.plugin = plugin;
		this.arenaName = plugin.getArenaDataString(arenaName, "name");
		secondsToWait = plugin.getArenaConfigInt(arenaName, "waitTime");
		teamScores.put("RED", 0);
		teamScores.put("BLUE", 0);
		
		Map<Material, String> arenaTools = new HashMap<Material, String>();
		arenaTools.put(Material.IRON_AXE, "Gravity Hammer");
		arenaTools.put(Material.DIAMOND_SWORD, "Energy Sword");
		for(Material mat : arenaTools.keySet()) {
			ItemStack itemToAdd = new ItemStack(mat, 1);
			ItemMeta itemToAddMeta = itemToAdd.getItemMeta();
			itemToAddMeta.setDisplayName(arenaTools.get(mat));
			itemToAdd.setItemMeta(itemToAddMeta);
			arenaStarterKit.add(itemToAdd);
		}
		/*if(plugin.getConfig().isConfigurationSection("starterKit") && plugin.getConfig().getConfigurationSection("starterKit").getKeys(false).size() > 0) {
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
		}*/
	}
	
	int queueTimer = -1;
	int roundGraceTimer = -1;
	public boolean nextRoundPhase = false;
	public int round = 1;
	Map<Player, Location> oldLocs = new HashMap<Player, Location>();
	Map<Player, ItemStack[]> oldInvs = new HashMap<Player, ItemStack[]>();
	Map<Player, Integer> oldXp = new HashMap<Player, Integer>();
	Map<Player, Double> oldHp = new HashMap<Player, Double>();
	Map<Player, Integer> oldFood = new HashMap<Player, Integer>();
	Map<Player, GameMode> oldGm = new HashMap<Player, GameMode>();
	
	public ItemStack getGrifblockItem() {
		ItemStack grifblockItem = new ItemStack(Material.GLOWSTONE, 1);
		ItemMeta meta = grifblockItem.getItemMeta();
		meta.setDisplayName("Grifblock");
		grifblockItem.setItemMeta(meta);
		return grifblockItem;
	}
	
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
				ply.getInventory().addItem(itemToAdd);
			}
		}
		this.getPlayerObj(ply).setTeamHelmet();
	}
	
	public void spawnGrifblock() {
		Location grifblockSpawn = plugin.getArenaBlockLocation(arenaName, "grifblockSpawn").add(0,1.5,0);
		this.grifblock = grifblockSpawn.getWorld().dropItem(grifblockSpawn, this.getGrifblockItem());
		this.grifblock.setVelocity(new Vector(0.0, 0.2, 0.0));
		this.grifblock.setGlowing(true);
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
		if(this.inProgress && (blueTeam.size() < 1 || redTeam.size() < 1)) {
			endArena();
		}
		if(round > plugin.getArenaConfigInt(arenaName, "roundsToPlay")) {
			if(teamScores.get("RED") > teamScores.get("BLUE"))
				winArena("RED");
			else
				winArena("BLUE");
		}
	}
	
	public void startArena() {
		inProgress = true;
        Bukkit.getScheduler().cancelTask(queueTimer);
        queueTimer = -1;
        secondsToWait = plugin.getArenaConfigInt(arenaName, "waitTime");
        messageAllPlayers("OF SALT");
    	//List<Double> telCords = plugin.getArenaCordData(arenaName, "playerSpawn");
    	//Location telLoc = new Location(plugin.getArenaWorld(arenaName), telCords.get(0), telCords.get(1)+1, telCords.get(2));
        //Location telLoc = plugin.getArenaBlockLocation(arenaName, "playerSpawn").add(-0.5, 0, -0.5);
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
        	
        	ply.getInventory().clear();
        	ply.setLevel(0);
        	ply.setHealth(20);
        	ply.setFoodLevel(20);
        	ply.setGameMode(GameMode.SURVIVAL);
        	
        	if(redTeam.size() > blueTeam.size()) {
        		blueTeam.put(ply, players.get(ply));
        		players.get(ply).assignTeam("BLUE");
        	}
        	else {
        		redTeam.put(ply, players.get(ply));
        		players.get(ply).assignTeam("RED");
        	}
        	ply.teleport(plugin.getArenaBlockLocation(arenaName, this.getPlayerObj(ply).getTeam()+"Spawn").add(0,1.5,0));
        	equipStarterKit(ply);
        	ply.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, (6000*20), 0));
        	//ply.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, (6000*20), 0));
        }
        this.updateScoreboards();
        startRound();
	}
	
	public void startRound() {
		nextRoundPhase = false;
		for(Player ply : players.keySet()) {
			getPlayerObj(ply).resetInArena();
		}
		if(round != 69)
			messageAllPlayers("Round " + round);
		else
			messageAllPlayers("Round " + round + ChatColor.ITALIC + " nice...");
		this.spawnGrifblock();
		updateSigns();
		updateScoreboards();
	}
	
	public void nextRound() {
		nextRoundPhase = true;
		round += 1;
		if(round <= plugin.getArenaConfigInt(arenaName, "roundsToPlay")) {
			messageAllPlayers("You have " + plugin.getArenaConfigInt(arenaName, "roundGraceTime") + " seconds before the next round starts...");
			roundGraceTimer = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					startRound();
				}
			}, plugin.getArenaConfigInt(arenaName, "roundGraceTime")*20);
		}
		checkToEnd();
	}
	
	public void winArena(String winTeam) {
		Map<Player, PGGrifblockPlayer> winTeamMap = new HashMap<Player, PGGrifblockPlayer>();
		if(winTeam.equals("RED"))
			winTeamMap = redTeam;
		else
			winTeamMap = blueTeam;
		for(Player ply : winTeamMap.keySet())
			this.bootPlayer(ply, "won");
		endArena();
	}
	
	public void endArena() {
		inProgress = false;
		round = 1;
		nextRoundPhase = false;
		updateSigns();
		this.bootAllPlayers();
		teamScores.put("RED", 0);
		teamScores.put("BLUE", 0);
		players.clear();
		blueTeam.clear();
		redTeam.clear();
		oldLocs.clear();
		oldInvs.clear();
		oldXp.clear();
		oldHp.clear();
		oldFood.clear();
		oldGm.clear();
		
		if(!grifblock.equals(null) && grifblock.isValid() && grifblock instanceof Item)
			grifblock.remove();
		
		Bukkit.getScheduler().cancelTask(roundGraceTimer);
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
	
	public void bootPlayer(Player ply) {
		bootPlayer(ply, "left");
	}
	public void bootPlayer(Player ply, String reason) {
		//addTopScore(ply, wave);
		//getPlayer(ply).clearPerks();
		if(getPlayerObj(ply).hasGrifblock()) {
			getPlayerObj(ply).toggleGrifblock();
			this.spawnGrifblock();
		}
		players.remove(ply);
		redTeam.remove(ply);
		blueTeam.remove(ply);
			
		updateScoreboards();
		ply.setGlowing(false);
		plugin.removeUnclaimedData(ply);
		ply.removePotionEffect(PotionEffectType.JUMP);
		ply.removePotionEffect(PotionEffectType.SLOW_FALLING);
		
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
		
		messageAllPlayers(ply.getName() + " has left!");
		
		plugin.writeMessage(ply, "You left!");
		ply.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		
		if(reason.equals("won")) {
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
			plugin.writeMessage(ply, "You won!");
		}
		checkToEnd();
	}
	
	public void bootAllPlayers() {
		List<Player> plys = new ArrayList<Player>(players.keySet());
		for(int i = plys.size()-1; i >= 0; i--) {
			Player ply = plys.get(i);
			bootPlayer(ply);
		}
		/*for(Player ply : players.keySet()) {
			plugin.printToConsole("booting " + ply.getName());
			bootPlayer(ply, false);
		}*/
		//endArena();
	}
}
