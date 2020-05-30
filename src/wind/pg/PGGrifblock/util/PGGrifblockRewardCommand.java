package wind.pg.PGGrifblock.util;

import org.bukkit.entity.Player;

import wind.pg.PGGrifblock.PGGrifblock;

public class PGGrifblockRewardCommand {
	PGGrifblock plugin;
	
	String commandString;
	String newString;
	
	String playerName;
	
	int wave;
	
	int survivedWaves;
	
	int survivedMult;
	int numMult;
	int indexOfMult;
	
	public PGGrifblockRewardCommand(PGGrifblock plugin, String commandString, Player player, int wave) {
		this.plugin = plugin;
		this.commandString = commandString;
		this.newString = commandString + " ";
		this.wave = wave;
		survivedWaves = wave - 1;
		if(newString.contains("#survivedMult")) {
			indexOfMult = newString.indexOf("#survivedMult");
			String multString = newString.substring(indexOfMult, newString.substring(indexOfMult).indexOf(" ")+newString.substring(0, indexOfMult).length());
			numMult = Integer.valueOf(multString.replace("#survivedMult", ""));
			survivedMult = survivedWaves*numMult;
			
			this.newString = this.newString.replace(multString, Integer.toString(survivedMult));
		}
		if(commandString.contains("#player")) {
			playerName = player.getName();
			
			this.newString = this.newString.replace("#player", playerName);
		}
		if(commandString.contains("#wave")) {
			this.newString = this.newString.replace("#wave", Integer.toString(wave));
		}
		if(commandString.contains("#survived")) {
			this.newString = this.newString.replace("#survived", Integer.toString(survivedWaves));
		}
	}
	
	public boolean notZero() {
		if(survivedMult == 0) {
			return false;
		}
		return true;
	}
	
	public String getNewCommandString() {
		return newString;
	}
}
