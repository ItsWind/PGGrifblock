package wind.pg.PGGrifblock.util;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import wind.pg.PGGrifblock.PGGrifblockArena;

public class PGGrifblockPlayerShieldTimer extends BukkitRunnable {
	Player ply;
	PGGrifblockArena arena;
	
	public PGGrifblockPlayerShieldTimer(Player ply, PGGrifblockArena arena) {
		this.ply = ply;
		this.arena = arena;
	}

	@Override
	public void run() {
		if(!arena.getPlayerObj(ply).hasGrifblock() || ply.getAbsorptionAmount() == 10.0) {
			this.cancel();
			return;
		}
	}

}
