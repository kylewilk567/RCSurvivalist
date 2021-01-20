package xyz.ravencraft.RCSurvivalist.Modes;


import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import xyz.ravencraft.RCSurvivalist.LangManager;
import xyz.ravencraft.RCSurvivalist.Main;
import xyz.ravencraft.RCSurvivalist.PlayerData.PlayerInfo;

public class Competitive implements Listener {

	/*
	 * This class is currently not in use!!! See vanilla (no-kits) gamemode
	 */
	
	Main plugin = Main.getPlugin(Main.class);
	LangManager lmang = LangManager.getLmang();
	PlayerInfo playerInfo = PlayerInfo.getPlayerInfo();
	
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		if(!playerInfo.isInSurvivalistGame(player)) return;
		if(!playerInfo.getLobbyFromPlayer(player).getGameType().equalsIgnoreCase("Competitive")) return;
		
		//Make sure game did not end
		int numSurvivalists = playerInfo.getLobbyFromPlayer(player).getAllTeams().get("Survivalist").size();
		int numHunters = playerInfo.getLobbyFromPlayer(player).getAllTeams().get("Hunter").size();
		if(numSurvivalists == 0 || numHunters == 0) return;
	}
}
