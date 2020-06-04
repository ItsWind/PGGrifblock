package wind.pg.PGGrifblock.events;

import java.util.Objects;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import wind.pg.PGGrifblock.PGGrifblock;

public class PGGrifblockPlayerJoinEvent implements Listener {
	PGGrifblock plugin;
	public PGGrifblockPlayerJoinEvent(PGGrifblock plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlyJoin(PlayerJoinEvent event) {
		Player ply = event.getPlayer();
		if(plugin.hasUnclaimedData(ply)) {
			FileConfiguration unclaimedData = plugin.getUnclaimedData(ply);
			Set<String> unclaimedDataKeys = unclaimedData.getKeys(false);
			for(String dataName : unclaimedDataKeys) {
				if(dataName.equalsIgnoreCase("oldLoc")) {
					Location telLoc = unclaimedData.getLocation("oldLoc");
					ply.teleport(telLoc);
				}
				else if(dataName.equalsIgnoreCase("oldInv")) {
					final ItemStack[] oldItems = Objects.requireNonNull(unclaimedData.getList("oldInv")).stream().map(o -> (ItemStack) o).toArray(ItemStack[]::new);
					ply.getInventory().setContents(oldItems);
				}
				else if(dataName.equalsIgnoreCase("oldXp")) {
					ply.setLevel(unclaimedData.getInt("oldXp"));
				}
				else if(dataName.equalsIgnoreCase("oldXpPer")) {
					ply.setExp((float) unclaimedData.getDouble("oldXpPer"));
				}
				else if(dataName.equalsIgnoreCase("oldHp")) {
					ply.setHealth(unclaimedData.getDouble("oldHp"));
				}
				else if(dataName.equalsIgnoreCase("oldFood")) {
					ply.setFoodLevel(unclaimedData.getInt("oldFood"));
				}
				else if(dataName.equalsIgnoreCase("oldGm")) {
					GameMode gmOld = GameMode.valueOf(unclaimedData.getString("oldGm"));
					ply.setGameMode(gmOld);
				}
				else if(dataName.equalsIgnoreCase("wasSpectating")) {
					ply.setGameMode(GameMode.SURVIVAL);
				}
				else if(dataName.equalsIgnoreCase("wasInvulnerable")) {
					ply.setInvulnerable(false);
				}
			}
			plugin.writeMessage(ply, "Your data has been restored! Maybe the server crashed as you were playing?");
			plugin.removeUnclaimedData(ply);
		}
	}
}
