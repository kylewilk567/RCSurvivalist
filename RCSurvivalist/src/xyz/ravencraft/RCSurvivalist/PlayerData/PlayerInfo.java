package xyz.ravencraft.RCSurvivalist.PlayerData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import xyz.ravencraft.RCSurvivalist.LangManager;
import xyz.ravencraft.RCSurvivalist.LobbyManager;
import xyz.ravencraft.RCSurvivalist.Main;
import xyz.ravencraft.RCSurvivalist.TeamsManager;
import xyz.ravencraft.RCSurvivalist.Kits.KitFileManager;

public class PlayerInfo {
	
	private Main plugin = Main.getPlugin(Main.class);
	LangManager lmang = LangManager.getLmang();
	KitFileManager kitMang = KitFileManager.getKitMang();
	PlayerDataManager playerMang = PlayerDataManager.getPlayerMang();
	LobbyManager lobbyMang = LobbyManager.getLobbyMang();
	
	private static PlayerInfo playerInfo;
	
	public static List<Player> inSurvivalistGame = new ArrayList<Player>();
	HashMap<String, String> playersLobbies = new HashMap<String, String>();

	
	/*
	 * gets the instance of playerinfo used in this plugin
	 */
	public static PlayerInfo getPlayerInfo() {
		if(playerInfo == null) {
			playerInfo = new PlayerInfo();
		}
		return playerInfo;
	}
	

	
	/*
	 * Gets list of players currently in a game (across all lobbies)
	 */
	public List<Player> getPlayersInAllGames(){
		return inSurvivalistGame;
	}
	
	/*
	 * Gets the Hashmap of playersLobbies
	 */
	public HashMap<String, String> getPlayerLobbyMap(){
		return playersLobbies;
	}
	
	/*
	 * Check if player is currently in a survivalist game! Used for checking if events should do anything
	 */
	public Boolean isInSurvivalistGame(Player player) {
		for(Player inList : inSurvivalistGame) {
			if(inList.getName().equalsIgnoreCase(player.getName())) return true;
		}
		
		return false;
	}

	/*
	 * Returns the active kit a player is using by checking permissions. 
	 * If they don't have any of the perms, the FIRST kit in the list is chosen as their kit.
	 */
	public String getPlayerKit(Player player) {
		//Check player permissions (get all kits. Make their kit the first permission that returns true)
		String playerkit = kitMang.getAllKits().get(0);
		for(String kit : kitMang.getAllKits()) {
			if(player.hasPermission("rcs." + kit)) {
				playerkit = kit;
				break;
			}
		}
		return playerkit;
	}
	
	/*
	 * Gets the time in days since a player has last been on the server. If error, returns 0.
	 */
	public int getLastLogin(Player player) {
		//Get player's UUID
		String uuid = player.getUniqueId().toString();
		//Check if player's file exists  and get the time long value in the file
		Long lastLogin;
		try{
			lastLogin = PlayerDataManager.getPlayerFile(plugin, uuid).getLong("LastLogin");
		} catch(Exception e) {
			e.printStackTrace();
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				lmang.getLang().getString("Error-CannotRetrieveFile")));
			return 0;
		}
		
		//Change that value to the correct time in days (using current time)
		lastLogin = System.currentTimeMillis() - lastLogin;
		int days = (int) (lastLogin / 86400000);
		return days;	
	}
	
	/*
	 * Returns TeamsManager class given player - made this since it was too hard to remember all the crap in that function below.
	 */
	public TeamsManager getLobbyFromPlayer(Player player) {
		return lobbyMang.getLobbies().get(playerInfo.getPlayerLobbyMap().get(player.getUniqueId().toString()));
	}
}
