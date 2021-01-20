package xyz.ravencraft.RCSurvivalist.Modes;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import xyz.ravencraft.RCSurvivalist.LangManager;
import xyz.ravencraft.RCSurvivalist.Main;
import xyz.ravencraft.RCSurvivalist.PlayerData.PlayerInfo;

public class Vanilla implements Listener {

	Main plugin = Main.getPlugin(Main.class);
	LangManager lmang = LangManager.getLmang();
	PlayerInfo playerInfo = PlayerInfo.getPlayerInfo();
	
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		if(!playerInfo.isInSurvivalistGame(player)) return;
		if(!playerInfo.getLobbyFromPlayer(player).getGameType().equalsIgnoreCase("Vanilla")) return;
		
		//Make sure game did not end
		int numSurvivalists = playerInfo.getLobbyFromPlayer(player).getAllTeams().get("Survivalist").size();
		int numHunters = playerInfo.getLobbyFromPlayer(player).getAllTeams().get("Hunter").size();
		if(numSurvivalists == 0 || numHunters == 0) return;
		
		//Set respawn location to that specified in their lobby OR check if they have bed
		if(player.getBedSpawnLocation() == null) event.setRespawnLocation(playerInfo.getLobbyFromPlayer(player).getRespawnLocation());
		else {
			event.setRespawnLocation(player.getBedSpawnLocation());
		}
		
		//If hunter, give a compass
		if(playerInfo.getLobbyFromPlayer(player).getPlayerTeam(player).equalsIgnoreCase("Hunter"))
			player.getInventory().addItem(new ItemStack(Material.COMPASS , 1));
		
		
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if(!playerInfo.isInSurvivalistGame(player)) {
			return;
		}
		if(playerInfo.getLobbyFromPlayer(player).getGameType().equalsIgnoreCase("Vanilla")) {
			//If player who died was a hunter, do nothing
			if (playerInfo.getLobbyFromPlayer(player).getPlayerTeam(player).equalsIgnoreCase("Hunter")) return;
			
			//Move player to hunter class
			playerInfo.getLobbyFromPlayer(player).moveToHunters(player);
			
			//Get number of survivalists left - game will end automatically if no more Survivalists in PluginEvents() class
			
			int secondsLeft = playerInfo.getLobbyFromPlayer(player).getTimer().getSecondsLeft();
			int numSurvivalists = playerInfo.getLobbyFromPlayer(player).getAllTeams().get("Survivalist").size();
			//Return if no survivalists left so that no messages are sent
			if(numSurvivalists == 0) return;
			int numHunters = playerInfo.getLobbyFromPlayer(player).getAllTeams().get("Hunter").size();
			int timeAdded = (int) ((38.73 / Math.sqrt((double) secondsLeft)) * 240.0 * (((double) numSurvivalists)/ ((double) numHunters)));
			if(timeAdded > 600) {
				timeAdded = 600;
			}
			playerInfo.getLobbyFromPlayer(player).getTimer().addSecondsLeft(timeAdded);

		}
	}
	
}
