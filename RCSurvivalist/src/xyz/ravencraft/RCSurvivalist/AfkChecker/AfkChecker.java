package xyz.ravencraft.RCSurvivalist.AfkChecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import VaultIntegration.VaultIntegration;
import net.milkbowl.vault.economy.Economy;
import xyz.ravencraft.MinigamesLobby.EndGameEvent;
import xyz.ravencraft.RCSurvivalist.LangManager;
import xyz.ravencraft.RCSurvivalist.LobbyManager;
import xyz.ravencraft.RCSurvivalist.Main;
import xyz.ravencraft.RCSurvivalist.TeamsManager;
import xyz.ravencraft.RCSurvivalist.AutoRegen.RegenData;
import xyz.ravencraft.RCSurvivalist.PlayerData.PlayerInfo;

public class AfkChecker implements Runnable {
	
	Main plugin = Main.getPlugin(Main.class);
	PlayerInfo playerInfo = PlayerInfo.getPlayerInfo();
	LangManager lmang = LangManager.getLmang();
	LobbyManager lobbyMang = LobbyManager.getLobbyMang();
	RegenData regendata = RegenData.getRegenData();
	VaultIntegration vaultInt = VaultIntegration.getVaultIntegration();
	Economy eco = vaultInt.getEconomy();
	HashMap<Player, Location> afkChecker = new HashMap<Player, Location>();
	int assignedTaskID;

	@Override
	public void run() {		
		//For each player
		List<Player> toBeRemoved = new ArrayList<Player>();
		for(Player player : playerInfo.getPlayersInAllGames()) {
			//Check if player is in afkChecker. If not, add them and continue to next player
			if(!afkChecker.containsKey(player)) {
				afkChecker.put(player, player.getLocation());
				continue;
			}
			
			//Check if player is not in a lobby (but still in InSurvivalistGame). This indicates the player quit the game
			if(playerInfo.getLobbyFromPlayer(player).getPlayerTeam(player) == null) continue;

			//Check the player's location and if it is the same as it was 5 minutes ago. Remove from game and teleport to end game location
			if(afkChecker.get(player).getBlockX() == player.getLocation().getBlockX() && afkChecker.get(player).getBlockY() == player.getLocation().getBlockY()
					&& afkChecker.get(player).getBlockZ() == player.getLocation().getBlockZ()) {
				playerInfo.getLobbyFromPlayer(player).getAllTeams().get(playerInfo.getLobbyFromPlayer(player).getPlayerTeam(player)).remove(player);
				toBeRemoved.add(player);
				player.getInventory().clear();
				player.getEquipment().clear();
				player.setHealth(20);
				player.setFoodLevel(20);
				player.setBedSpawnLocation(null);
				player.teleport(playerInfo.getLobbyFromPlayer(player).getEndGameLocation());
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
			lmang.getLang().getString("Afk-kicked")));
			}
			//If location not the same, update their location
			afkChecker.put(player, player.getLocation());
			
		}
		for(Player player : toBeRemoved) {
			playerInfo.getPlayersInAllGames().remove(player);
		}
		
		//Check if game should end now
		for(Map.Entry<String,TeamsManager> entry : lobbyMang.getLobbies().entrySet()) {
			int numSurvivalists = entry.getValue().getAllTeams().get("Survivalist").size();
			int numHunters = entry.getValue().getAllTeams().get("Hunter").size();
			if(entry.getValue().gameInProgress()) {
			if(numSurvivalists == 0 || numHunters == 0) {
				
				//Get all the players in their lobby and remove them from survivalist game
				for(Player TBRemoved : entry.getValue().getAllPlayersInLobby()) {
					if(playerInfo.isInSurvivalistGame(TBRemoved)) {
						playerInfo.getPlayersInAllGames().remove(TBRemoved);
						TBRemoved.setBedSpawnLocation(null);
						TBRemoved.getInventory().clear();
						TBRemoved.getEquipment().clear();
						TBRemoved.setHealth(20);
						TBRemoved.setFoodLevel(20);
						eco.depositPlayer(TBRemoved, 75.0);
						TBRemoved.teleport(playerInfo.getLobbyFromPlayer(TBRemoved).getEndGameLocation());
						if(numSurvivalists == 0) TBRemoved.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
					lmang.getLang().getString("Hunters-win")));
						if(numHunters == 0) TBRemoved.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
								lmang.getLang().getString("Hunters-quit")));
						Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Removed " + TBRemoved.getName() + " from game!");
					}
				}
				
				List<Player> toBeRemoved2 = new ArrayList<Player>();
				String lobbyName = entry.getValue().getSurvivalistLobby().getLobbyName();
				for(Player testPlayer : playerInfo.getPlayersInAllGames()) {
					if(playerInfo.getLobbyFromPlayer(testPlayer).getSurvivalistLobby().getLobbyName().equalsIgnoreCase(lobbyName)) {
						if(playerInfo.isInSurvivalistGame(testPlayer)) toBeRemoved2.add(testPlayer);
					}
				}
				for(Player testPlayer2 : toBeRemoved2) {
					playerInfo.getPlayersInAllGames().remove(testPlayer2);
				}
				//Call End Game event
				Bukkit.getPluginManager().callEvent(new EndGameEvent(entry.getValue().getSurvivalistLobby()));
				entry.getValue().getTimer().stopTimer();
				regendata.regenerateChunks(entry.getValue());
				return;
				
			}}
		}

	}
	
	public void scheduleTimer() {
		this.assignedTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 10L, 6000L); //repeats every 5 minutes
		Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "AfkChecker started!");
	}
	
	public void stopTimer() {
		Bukkit.getScheduler().cancelTask(assignedTaskID);
	}

}
