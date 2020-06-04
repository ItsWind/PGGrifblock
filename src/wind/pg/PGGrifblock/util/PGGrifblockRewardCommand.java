package wind.pg.PGGrifblock.util;

import org.bukkit.entity.Player;

import wind.pg.PGGrifblock.PGGrifblock;

public class PGGrifblockRewardCommand {
	PGGrifblock plugin;
	
	String commandString;
	String newString;
	
	String playerName;
	
	int round;
	
	int roundMult;
	int numMult;
	int indexOfMult;
	
	public PGGrifblockRewardCommand(PGGrifblock plugin, String commandString, Player player, int round) {
		this.plugin = plugin;
		this.commandString = commandString;
		this.newString = commandString + " ";
		this.round = round;
		if(newString.contains("#roundMult")) {
			indexOfMult = newString.indexOf("#roundMult");
			String multString = newString.substring(indexOfMult, newString.substring(indexOfMult).indexOf(" ")+newString.substring(0, indexOfMult).length());
			numMult = Integer.valueOf(multString.replace("#roundMult", ""));
			roundMult = round*numMult;
			
			this.newString = this.newString.replace(multString, Integer.toString(roundMult));
		}
		if(commandString.contains("#player")) {
			playerName = player.getName();
			
			this.newString = this.newString.replace("#player", playerName);
		}
		if(commandString.contains("#round")) {
			this.newString = this.newString.replace("#round", Integer.toString(round));
		}
	}
	
	public boolean notZero() {
		if(roundMult == 0) {
			return false;
		}
		return true;
	}
	
	public String getNewCommandString() {
		return newString;
	}
}
