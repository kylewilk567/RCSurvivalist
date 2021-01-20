package xyz.ravencraft.RCSurvivalist;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;

import VaultIntegration.VaultIntegration;
import net.milkbowl.vault.economy.Economy;
import net.minecraft.server.v1_16_R1.MinecraftServer;
import xyz.ravencraft.MinigamesLobby.EndGameEvent;
import xyz.ravencraft.RCSurvivalist.AutoRegen.RegenData;
import xyz.ravencraft.RCSurvivalist.CompassTracking.SelectorGUI;
import xyz.ravencraft.RCSurvivalist.PlayerData.PlayerDataManager;
import xyz.ravencraft.RCSurvivalist.PlayerData.PlayerInfo;

public class PluginEvents implements Listener {

	Main plugin = Main.getPlugin(Main.class);
	LangManager lmang = new LangManager();
	PlayerDataManager playerMang = PlayerDataManager.getPlayerMang();
	PlayerInfo playerInfo = PlayerInfo.getPlayerInfo();
	LobbyManager lobbyMang = LobbyManager.getLobbyMang();
	SelectorGUI trackerGUI = new SelectorGUI();
	RegenData regendata = RegenData.getRegenData();
	VaultIntegration vaultInt = VaultIntegration.getVaultIntegration();
	Economy eco = vaultInt.getEconomy();
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {	
		Player player = event.getPlayer();
		
		//If player has a file, update their LastLogin time to current time
		try {
		playerMang.createPlayerFile(player.getUniqueId().toString());
		YamlConfiguration playerFile = PlayerDataManager.getPlayerFile(plugin, event.getPlayer().getUniqueId().toString());
		playerFile.set("LastLogin", System.currentTimeMillis());
		playerFile.save(new File(plugin.getDataFolder().getPath() + File.separator + "PlayerData" + File.separator + player.getUniqueId().toString() + ".yml"));
		
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		//Check if player is in the rejoin list. If so, give them the option to spawn back...
		if(!playerInfo.isInSurvivalistGame(event.getPlayer())) {
			return;
		}
		
		//Allow them to open the Rejoin GUI
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new RejoinDelay(player), 40L);	
		
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		
		
		//Check if player is in SurvivalistGame
		if(!playerInfo.isInSurvivalistGame(event.getPlayer())) {
			return;
		}
		
		//Change tracking map BEFORE removing from team
		playerInfo.getLobbyFromPlayer(player).changeTrackingMap(player);
		//Player left a game. They MUST rejoin as a hunter to prevent abuse
		//Remove player from team
		playerInfo.getLobbyFromPlayer(player).getAllTeams().get(playerInfo.getLobbyFromPlayer(player).getPlayerTeam(player)).remove(player); 

		
		//Check if game will end
		int numSurvivalists = playerInfo.getLobbyFromPlayer(player).getAllTeams().get("Survivalist").size();
		int numHunters = playerInfo.getLobbyFromPlayer(player).getAllTeams().get("Hunter").size();
		if(numSurvivalists == 0 || numHunters == 0) {
			//Get all the players in their lobby and remove them from survivalist game
			//NOTE: Test WITHOUT doing ANYTHING to playerLobbies and LobbyManager maps as I believe all these will be replaced upon new games anyways.
			for(Player TBRemoved : playerInfo.getLobbyFromPlayer(player).getAllPlayersInLobby()) {
				if(playerInfo.isInSurvivalistGame(TBRemoved)) {
					playerInfo.getPlayersInAllGames().remove(TBRemoved);
					TBRemoved.setBedSpawnLocation(null);
					TBRemoved.getInventory().clear();
					TBRemoved.getEquipment().clear();
					TBRemoved.setHealth(20);
					TBRemoved.setFoodLevel(20);
					eco.depositPlayer(TBRemoved, 35.0);
					TBRemoved.teleport(playerInfo.getLobbyFromPlayer(TBRemoved).getEndGameLocation());
					
					TBRemoved.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				lmang.getLang().getString("Hunters-win")));
					Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Removed " + TBRemoved.getName() + " from game!");
				}
			}
			//Also remove the player who quit from InSurvivalistGame
			playerInfo.getPlayersInAllGames().remove(player);
			//Call End Game event
			Bukkit.getPluginManager().callEvent(new EndGameEvent(playerInfo.getLobbyFromPlayer(player).getSurvivalistLobby()));
			regendata.regenerateChunks(playerInfo.getLobbyFromPlayer(player));
			//Stop the timer/compass updating***
			playerInfo.getLobbyFromPlayer(player).getTimer().stopTimer();
			return;
		}
		

	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(!playerInfo.isInSurvivalistGame(event.getPlayer())) {
			return;
		}
		Player player = event.getPlayer();
		
		//Player left a game through a command. Remove from the PlayerInfo list (no chance of rejoining)
		if(event.getCause() == TeleportCause.COMMAND) {
			playerInfo.getPlayersInAllGames().remove(event.getPlayer());
			playerInfo.getLobbyFromPlayer(player).getAllTeams().get(playerInfo.getLobbyFromPlayer(player).getPlayerTeam(player)).remove(player);
			
			//Check if game will end
			int numSurvivalists = playerInfo.getLobbyFromPlayer(player).getAllTeams().get("Survivalist").size();
			int numHunters = playerInfo.getLobbyFromPlayer(player).getAllTeams().get("Hunter").size();
			if(numSurvivalists == 0 || numHunters == 0) {
				//Get all the players in their lobby and remove them from survivalist game
				//NOTE: Test WITHOUT doing ANYTHING to playerLobbies and LobbyManager maps as I believe all these will be replaced upon new games anyways.
				for(Player TBRemoved : playerInfo.getLobbyFromPlayer(player).getAllPlayersInLobby()) {
					if(playerInfo.isInSurvivalistGame(TBRemoved)) {
						playerInfo.getPlayersInAllGames().remove(TBRemoved);
						TBRemoved.setBedSpawnLocation(null);
						TBRemoved.getInventory().clear();
						TBRemoved.getEquipment().clear();
						TBRemoved.setHealth(20);
						TBRemoved.setFoodLevel(20);
						eco.depositPlayer(TBRemoved, 35.0);
						if(TBRemoved.equals(player)) TBRemoved.spigot().respawn();
						TBRemoved.teleport(playerInfo.getLobbyFromPlayer(TBRemoved).getEndGameLocation());
						TBRemoved.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
					lmang.getLang().getString("Hunters-win")));
						Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Removed " + TBRemoved.getName() + " from game!");
					}
				}
				List<Player> toBeRemoved = new ArrayList<Player>();
				String lobbyName = playerInfo.getLobbyFromPlayer(player).getSurvivalistLobby().getLobbyName();
				for(Player testPlayer : playerInfo.getPlayersInAllGames()) {
					if(playerInfo.getLobbyFromPlayer(testPlayer).getSurvivalistLobby().getLobbyName().equalsIgnoreCase(lobbyName)) {
						if(playerInfo.isInSurvivalistGame(testPlayer)) toBeRemoved.add(testPlayer);
					}
				}
				for(Player testPlayer2 : toBeRemoved) {
					playerInfo.getPlayersInAllGames().remove(testPlayer2);
				}
				//Call End Game event
				Bukkit.getPluginManager().callEvent(new EndGameEvent(playerInfo.getLobbyFromPlayer(player).getSurvivalistLobby()));
				//regen chunks
				regendata.regenerateChunks(playerInfo.getLobbyFromPlayer(player));
				
				//Stop the timer/compass updating***
				playerInfo.getLobbyFromPlayer(player).getTimer().stopTimer();
				return;
			}
		}
	}
	
	@EventHandler
	public void onHit(EntityDamageByEntityEvent event) {
		
		//Check if entity damaged is player
		if(event.getEntityType() != EntityType.PLAYER) {
			return;
		}
		if(event.getDamager().getType() != EntityType.PLAYER) return;
		
		//Check if player is in a survivalist game
		if(!playerInfo.isInSurvivalistGame((Player) event.getEntity())) {
			return;
		}
		
		//Check if entity and damager are on the same team
		String entityLobbyName = playerInfo.getPlayerLobbyMap().get(((Player) event.getEntity()).getUniqueId().toString());
		String damagerLobbyName = playerInfo.getPlayerLobbyMap().get(((Player) event.getDamager()).getUniqueId().toString());
		if(lobbyMang.getLobbies().get(entityLobbyName).getPlayerTeam((Player) event.getEntity()).equalsIgnoreCase
				(lobbyMang.getLobbies().get(damagerLobbyName).getPlayerTeam((Player) event.getDamager()))) {
			
			//Entities are on the same team
			event.setCancelled(true);
		}
		
	}
	
	//Restricts the creation of any portals when player is in survivalist game. MUST use BlockPlaceEvent... rip
	@EventHandler
	public void onPortalCreation(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if(playerInfo.isInSurvivalistGame(player)) {
			Block placed = event.getBlockPlaced();
			Block placedOn = event.getBlockAgainst();
			if(placed.getType() == Material.FIRE && placedOn.getType() == Material.OBSIDIAN) {
				event.setCancelled(true);
			}
		}
		
	}
	
	/*
	 * Compass Survivalist/Hunter Selector
	 */
	@EventHandler
	public void onCompassRightClick(PlayerInteractEvent event) {
		Action action = event.getAction();
		Player player = event.getPlayer();
		EquipmentSlot e = event.getHand();
		if(e == null) return;
		if(!e.equals(EquipmentSlot.HAND)) return;
		
		//Do nothing if not in survivalist game
		if(!playerInfo.isInSurvivalistGame(player)) {
			return;
		}

		//Main hand check - prevents right click from firing twice
		if(e.equals(EquipmentSlot.HAND)) {
		//Check the item in their hand, then check the action (Perhaps performance improvement)
		if(player.getInventory().getItemInMainHand().getType() == Material.COMPASS) {
			if(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)) {
				String team = lobbyMang.getLobbies().get(playerInfo.getPlayerLobbyMap().get(player.getUniqueId().toString()))
						.getPlayerTeam(player);
				if(!team.equalsIgnoreCase("Survivalist")) {
					Inventory inv = trackerGUI.createTrackingGUI(player, 
							lobbyMang.getLobbies().get(playerInfo.getPlayerLobbyMap().get(player.getUniqueId().toString())));
					event.getPlayer().openInventory(inv);
				}
			}
		}
	}
}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		//Do nothing if not in survivalist game
		if(!playerInfo.isInSurvivalistGame(player)) {
			return;
		}
		playerInfo.getLobbyFromPlayer(player).moveToHunters(player);
		
		int numSurvivalists = playerInfo.getLobbyFromPlayer(player).getAllTeams().get("Survivalist").size();
		int numHunters = playerInfo.getLobbyFromPlayer(player).getAllTeams().get("Hunter").size();
		if(numSurvivalists == 0 || numHunters == 0) {
			//Get all the players in their lobby and remove them from survivalist game
			//NOTE: Test WITHOUT doing ANYTHING to playerLobbies and LobbyManager maps as I believe all these will be replaced upon new games anyways.
			for(Player TBRemoved : playerInfo.getLobbyFromPlayer(player).getAllPlayersInLobby()) {
				if(playerInfo.isInSurvivalistGame(TBRemoved)) {
					playerInfo.getPlayersInAllGames().remove(TBRemoved);
					TBRemoved.setBedSpawnLocation(null);
					TBRemoved.getInventory().clear();
					TBRemoved.getEquipment().clear();
					TBRemoved.setHealth(20);
					TBRemoved.setFoodLevel(20);
					eco.depositPlayer(TBRemoved, 45.0);
					if(!TBRemoved.equals(player)) TBRemoved.teleport(playerInfo.getLobbyFromPlayer(TBRemoved).getEndGameLocation());
					//Respawn last survivalist who died on a half second delay
					if(TBRemoved.equals(player)) Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
							new RespawnDelay(playerInfo.getLobbyFromPlayer(TBRemoved), TBRemoved), 10L);
					TBRemoved.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				lmang.getLang().getString("Hunters-win")));
					
					Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Removed " + TBRemoved.getName() + " from game!");
				}
			}
			List<Player> toBeRemoved = new ArrayList<Player>();
			String lobbyName = playerInfo.getLobbyFromPlayer(player).getSurvivalistLobby().getLobbyName();
			for(Player testPlayer : playerInfo.getPlayersInAllGames()) {
				if(playerInfo.getLobbyFromPlayer(testPlayer).getSurvivalistLobby().getLobbyName().equalsIgnoreCase(lobbyName)) {
					if(playerInfo.isInSurvivalistGame(testPlayer)) toBeRemoved.add(testPlayer);
				}
			}
			for(Player testPlayer2 : toBeRemoved) {
				playerInfo.getPlayersInAllGames().remove(testPlayer2);
			}
			//Call End Game event
			Bukkit.getPluginManager().callEvent(new EndGameEvent(playerInfo.getLobbyFromPlayer(player).getSurvivalistLobby()));
			//regen chunks
			regendata.regenerateChunks(playerInfo.getLobbyFromPlayer(player));
			playerInfo.getLobbyFromPlayer(player).getTimer().stopTimer();
			return;
		}
		playerInfo.getLobbyFromPlayer(player).changeTrackingMap(player);
	}
	/*
	 * Used to prevent fall damage in the first 20 seconds of a game (when being dropped)
	 */
	@EventHandler
	public void onFallDamage(EntityDamageEvent event) {
			if(event.getEntityType() != null && event.getEntityType() == EntityType.PLAYER) {
			if(event.getCause() == DamageCause.FALL) {
				Player player = (Player) event.getEntity();
				if(!playerInfo.isInSurvivalistGame(player)) return;
				if(playerInfo.getLobbyFromPlayer(player).getNoFallDamagePlayers().contains(player)) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onPhantomSpawn(CreatureSpawnEvent event) {
		if(plugin.getConfig().getBoolean("monsters.spawn.phantom")) return;
		if(event.getEntityType() == EntityType.PHANTOM) {
			event.setCancelled(true);
			return;
		}
	}
}
