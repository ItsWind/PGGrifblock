package wind.pg.PGGrifblock;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;

public class PGGrifblockPlayer {
	PGGrifblock plugin;
	PGGrifblockArena arena;
	String team;
	Player ply;
	
	public PGGrifblockPlayer(PGGrifblock plugin, PGGrifblockArena arena, Player ply) {
		this.plugin = plugin;
		this.arena = arena;
		this.ply = ply;
	}
	
	boolean hasGrifblock = false;
	
	public void setTeamHelmet() {
		plugin.printToConsole("putting helmet");
		ItemStack helmetItem = new ItemStack(Material.LEATHER_HELMET, 1);
		LeatherArmorMeta itemMeta = (LeatherArmorMeta) helmetItem.getItemMeta();
		if(team.equals("RED")) {
			plugin.printToConsole("red helmet");
			itemMeta.setColor(Color.fromRGB(255, 0, 0));
		}
		else {
			plugin.printToConsole("blue helmet");
			itemMeta.setColor(Color.fromRGB(0, 0, 255));
		}
		plugin.printToConsole("setting helmet");
		helmetItem.setItemMeta(itemMeta);
		plugin.printToConsole("on helmet");
		ply.getInventory().setHelmet(helmetItem);
	}
	
	public String getTeam() {
		return team;
	}
	
	public void assignTeam(String team) {
		this.team = team;
	}
	
	public boolean hasGrifblock() {
		return hasGrifblock;
	}
	
	public void toggleGrifblock() {
		hasGrifblock = !hasGrifblock;
		if(hasGrifblock) {
			arena.grifblock = ply;
			ply.setGlowing(true);
			ply.getInventory().clear();
        	ply.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (6000*20), 0));
        	ply.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(30.0);
        	ply.setHealth(30.0);
		}
		else {
			ply.setGlowing(false);
			ply.getInventory().remove(Material.GLOWSTONE);
			ply.removePotionEffect(PotionEffectType.SPEED);
        	ply.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        	ply.setHealth(20.0);
			//arena.equipStarterKit(ply);
		}
	}
	
	public void dropGrifblock() {
		if(hasGrifblock()) {
			toggleGrifblock();
			arena.grifblock = ply.getWorld().dropItemNaturally(ply.getLocation(), arena.getGrifblockItem());
			arena.grifblock.setGlowing(true);
		}
	}
	
	public void updateScoreboard() {
		if(plugin.getConfig().getBoolean("showScoreboard")) {
			ScoreboardManager manager = Bukkit.getScoreboardManager();
			Scoreboard scoreboard = manager.getNewScoreboard();
			Objective waveTitle = scoreboard.registerNewObjective("rounds", "dummy", ChatColor.RED + "Round " + arena.round);
			waveTitle.setDisplaySlot(DisplaySlot.SIDEBAR);
			for(Player ply : arena.players.keySet()) {
				String plyName = ply.getName();
				Map<String, ChatColor> colors = new HashMap<String, ChatColor>();
				colors.put("BLUE", ChatColor.BLUE);
				colors.put("RED", ChatColor.RED);
				if(arena.getPlayerObj(ply).getTeam() != null)
					plyName = colors.get(arena.getPlayerObj(ply).getTeam()) + ply.getName();
				Score score = waveTitle.getScore(plyName);
				score.setScore(ply.getLevel());
			}
			Score scoresDivider = waveTitle.getScore("-----------");
			scoresDivider.setScore(-1);
			Score redScore = waveTitle.getScore(ChatColor.RED + "RED" + ChatColor.RESET + ": " + ChatColor.YELLOW + arena.teamScores.get("RED"));
			redScore.setScore(-2);
			Score blueScore = waveTitle.getScore(ChatColor.BLUE + "BLUE" + ChatColor.RESET + ": " + ChatColor.YELLOW + arena.teamScores.get("BLUE"));
			blueScore.setScore(-3);
			ply.setScoreboard(scoreboard);
		}
	}
	
	public void resetInArena() {
		if(plugin.playerIsPlaying(ply) != null) {
			PGGrifblockArena arena = plugin.playerIsPlaying(ply);
			//Location spawnLoc = new Location(arena.arenaWorld, plugin.getArenaCordData(arena.arenaName, "playerSpawn").get(0), plugin.getArenaCordData(arena.arenaName, "playerSpawn").get(1)+1, plugin.getArenaCordData(arena.arenaName, "playerSpawn").get(2));
			Location spawnLoc = plugin.getArenaBlockLocation(arena.arenaName, this.team+"Spawn").add(0,1.5,0);
			ply.getInventory().clear();
			arena.equipStarterKit(ply);
        	ply.setHealth(20);
			ply.setFireTicks(0);
			dropGrifblock();
			ply.teleport(spawnLoc);
			ply.setVelocity(new Vector(0,0,0));
		}
	}
}
