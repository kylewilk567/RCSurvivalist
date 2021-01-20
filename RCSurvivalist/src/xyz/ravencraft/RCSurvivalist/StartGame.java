package xyz.ravencraft.RCSurvivalist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import xyz.ravencraft.MinigamesLobby.StartGameEvent;
import xyz.ravencraft.RCSurvivalist.AutoRegen.RegenData;
import xyz.ravencraft.RCSurvivalist.Kits.KitFileManager;
import xyz.ravencraft.RCSurvivalist.PlayerData.PlayerDataManager;
import xyz.ravencraft.RCSurvivalist.PlayerData.PlayerInfo;

public class StartGame implements Listener {
	
	HashMap<String, List<Player>> teams = new HashMap<String, List<Player>>();
	Main plugin = Main.getPlugin(Main.class);
	PlayerDataManager playerMang = PlayerDataManager.getPlayerMang();
	LobbyManager lobbyMang = LobbyManager.getLobbyMang();
	RegenData regendata = RegenData.getRegenData();
	
	//This Event listens to the MinigamesLobby plugin and takes all the data from that lobby (Players, respective teams, etc)
	@EventHandler
	public void onStartGame(StartGameEvent event) {	
		//Get Gamemode. If Gamemode is NOT Survivalist, do nothing!
		if(!event.getLobby().getGameMode().equalsIgnoreCase("Survivalist")) {
			return;
		}
		
		teams = event.getLobby().getTeams();
		//Get all players on all teams and create a file for them if needed
		for(Map.Entry<String, List<Player>> entry : teams.entrySet()) { //This is how you cycle through a hashmap
			for(Player player : entry.getValue()) {
				//Also add all players in the lobby to the PlayerInfo List - used for checking if player is currently in a game
				PlayerInfo.inSurvivalistGame.add(player);
				
				//Also add all players to the PlayerInfo HashMap - used for retrieving the lobbyName given a player
				PlayerInfo.getPlayerInfo().getPlayerLobbyMap().put(player.getUniqueId().toString(), event.getLobby().getLobbyName());
			}
		}

		
		//Initialize teams and create a new instance.
		TeamsManager teamMang = new TeamsManager();
		teamMang.initializeTeamsManager(teams, event.getLobby().getRespawnLocation(), 
				event.getLobby().getEndGameLocation(), event.getLobby().getGameType(), event.getLobby());
		teamMang.getTimer().scheduleTimer();
		lobbyMang.addLobby(event.getLobby().getLobbyName(), teamMang);
		
		//Initialize Chunk Regen Data
		regendata.getRegenManager().put(teamMang, new ArrayList<Location>());

		
		
		//Give all players no fall damage and fire resistance for 20 seconds
		for(Player player : teamMang.getAllPlayersInLobby()) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 400, 1));
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new RemoveFallDamage(teamMang), 900L);
		}
		
		//Give all hunters a compass
		for(Player player : teamMang.getAllPlayersInLobby()) {
			if(teamMang.getPlayerTeam(player).equalsIgnoreCase("Hunter")) {
				player.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
			}
		}
		
		//Give kits to players based on their permissions and configurations (if casual gamemode)
		if(teamMang.getGameType().equalsIgnoreCase("Casual")) {
			for(Player player : teamMang.getAllPlayersInLobby()) {
				String kitName = PlayerInfo.getPlayerInfo().getPlayerKit(player);
				//Get kitlevel from playerDataFile
				int level = PlayerDataManager.getPlayerFile(plugin, player.getUniqueId().toString()).getInt("kits." + kitName);
				@SuppressWarnings("unchecked")
				ItemStack[] contents = ((List<ItemStack>) KitFileManager.getKitFile(plugin, kitName).get(Integer.toString(level))).toArray(new ItemStack[0]);
				for(ItemStack item : contents) {
					if(item == null) continue;
					player.getInventory().addItem(item);
				}
			}
		}
		
		//Check gamemode type and give players respective effects
		for(Player player : teamMang.getAllPlayersInLobby()) {
			if(teamMang.getPlayerTeam(player).equalsIgnoreCase("Hunter")) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 800, 2));
				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 800, 2));
			}
			else {
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 800, 3));
				player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 800, 3));
			}
		}
		
	}

}
