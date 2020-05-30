package wind.pg.PGGrifblock;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class PGGrifblockPlayer {
	PGGrifblock plugin;
	PGGrifblockArena arena;
	Player ply;
	
	public PGGrifblockPlayer(PGGrifblock plugin, PGGrifblockArena arena, Player ply) {
		this.plugin = plugin;
		this.arena = arena;
		this.ply = ply;
	}
	
	public void updateScoreboard() {
		if(plugin.getConfig().getBoolean("showScoreboard")) {
			ScoreboardManager manager = Bukkit.getScoreboardManager();
			Scoreboard scoreboard = manager.getNewScoreboard();
			Objective waveTitle = scoreboard.registerNewObjective("rounds", "dummy", ChatColor.RED + "Round " + arena.round);
			waveTitle.setDisplaySlot(DisplaySlot.SIDEBAR);
			for(Player ply : arena.players.keySet()) {
				Score score = waveTitle.getScore(ply.getName());
				score.setScore(ply.getLevel());
			}
			ply.setScoreboard(scoreboard);
		}
	}
	
	
}
