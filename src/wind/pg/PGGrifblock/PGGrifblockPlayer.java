package wind.pg.PGGrifblock;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
	
	int plyDeathTimer = -1;
	int deadSecondsLeft = 4;
	
	boolean hasGrifblock = false;
	int grifblockShieldDamageTimer = -1;
	int grifblockShieldRegenTimer = -1;
	
	int attackCooldownTimer = -1;
	double attackCooldown = 1.0;
	
	public double getAttackCooldown() {
		return attackCooldown;
	}
	
	public void attack() {
		if(plugin.getArenaConfigInt(arena.arenaName, "playerAttackCooldown") > 0) {
			attackCooldown = 0.0;
			ply.setExp((float) attackCooldown);
			if(attackCooldownTimer != -1)
				Bukkit.getScheduler().cancelTask(attackCooldownTimer);
			attackCooldownTimer = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
				@Override
				public void run() {
					if(plugin.playerIsPlaying(ply) != null && attackCooldown < 1.0) {
						attackCooldown += 0.1;
						if(attackCooldown > 1.0)
							attackCooldown = 1.0;
						ply.setExp((float) attackCooldown);
					}
					else {
						attackCooldown = 1.0;
						Bukkit.getScheduler().cancelTask(attackCooldownTimer);
						attackCooldownTimer = -1;
					}
				}
			}, 2*(plugin.getArenaConfigInt(arena.arenaName, "playerAttackCooldown")), 2*(plugin.getArenaConfigInt(arena.arenaName, "playerAttackCooldown")));
		}
	}
	
	public void die() {
		this.dropGrifblock();
		ply.setGameMode(GameMode.SPECTATOR);
		ply.getInventory().clear();
		plyDeathTimer = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				if(!arena.nextRoundPhase && plugin.playerIsPlaying(ply) != null) {
					deadSecondsLeft -= 1;
					if(deadSecondsLeft <= 0) {
						ply.playSound(ply.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2.0F, 2.0F);
						resetInArena();
						Bukkit.getScheduler().cancelTask(plyDeathTimer);
						plyDeathTimer = -1;
						deadSecondsLeft = 4;
					}
					else {
						ply.playSound(ply.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2.0F, 1.3F);
					}
				}
				else {
					Bukkit.getScheduler().cancelTask(plyDeathTimer);
					plyDeathTimer = -1;
					deadSecondsLeft = 4;
				}
			}
		}, 1*20, 1*20);
	}
	
	public void setShieldTimer() {
		if(this.hasGrifblock()) {
			if(grifblockShieldDamageTimer != -1)
				Bukkit.getScheduler().cancelTask(grifblockShieldDamageTimer);
			grifblockShieldDamageTimer = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					grifblockShieldDamageTimer = -1;
					regenShield();
				}
			}, 5*20);
		}
	}
	private void regenShield() {
		if(this.hasGrifblock()) {
			if(grifblockShieldRegenTimer != -1)
				Bukkit.getScheduler().cancelTask(grifblockShieldRegenTimer);
			grifblockShieldRegenTimer = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
				@Override
				public void run() {
					if(hasGrifblock() && grifblockShieldDamageTimer == -1 && ply.getAbsorptionAmount() < 10.0) {
						if(ply.getAbsorptionAmount() >= 9.0) {
							for(int i = 0; i < 8; i++)
								ply.getWorld().spawnParticle(Particle.HEART, ply.getLocation().clone().add(plugin.getRandDouble(), plugin.getRandDouble(), plugin.getRandDouble()), 1);
							ply.getWorld().playSound(ply.getLocation(), Sound.BLOCK_BELL_RESONATE, 2.0F, 2.0F);
						}
						if(ply.getAbsorptionAmount()+1 > 10.0)
							ply.setAbsorptionAmount(10.0);
						else
							ply.setAbsorptionAmount(ply.getAbsorptionAmount()+1);
					}
					else {
						Bukkit.getScheduler().cancelTask(grifblockShieldRegenTimer);
						grifblockShieldRegenTimer = -1;
					}
				}
			}, 0, 1*20);
		}
	}
	
	public void setTeamHelmet() {
		ItemStack helmetItem = new ItemStack(Material.LEATHER_HELMET, 1);
		LeatherArmorMeta itemMeta = (LeatherArmorMeta) helmetItem.getItemMeta();
		if(team.equals("RED")) {
			itemMeta.setColor(Color.fromRGB(255, 0, 0));
		}
		else {
			itemMeta.setColor(Color.fromRGB(0, 0, 255));
		}
		helmetItem.setItemMeta(itemMeta);
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
			this.setTeamHelmet();
        	ply.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (6000*20), 0));
        	ply.setAbsorptionAmount(10.0);
		}
		else {
			ply.setGlowing(false);
			ply.getInventory().remove(Material.GLOWSTONE);
			ply.removePotionEffect(PotionEffectType.SPEED);
			ply.setAbsorptionAmount(0);
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
			ply.teleport(spawnLoc);
			ply.setGameMode(GameMode.SURVIVAL);
			ply.setVelocity(new Vector(0,0,0));
        	ply.setHealth(20);
    		ply.setAbsorptionAmount(0.0);
			plugin.extinguishPly(ply);
		}
	}
}
