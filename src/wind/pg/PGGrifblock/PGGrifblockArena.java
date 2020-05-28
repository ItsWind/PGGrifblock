package wind.pg.PGGrifblock;

public class PGGrifblockArena {
	PGGrifblock plugin;
	public boolean inProgress = false;
	public String arenaName;
	
	public PGGrifblockArena(PGGrifblock plugin, String arenaName) {
		this.plugin = plugin;
		this.arenaName = plugin.getArenaDataString(arenaName, "name");
	}
	
	int queueTimer = -1;
	
	public String getName() {
		return arenaName;
	}
}
