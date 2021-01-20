package xyz.ravencraft.RCSurvivalist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import xyz.ravencraft.MinigamesLobby.Lobby;
import xyz.ravencraft.RCSurvivalist.CompassTracking.UpdateTrackingCompass;

public class TeamsManager {
	Main plugin = Main.getPlugin(Main.class);
	HashMap<String, List<Player>> teams = new HashMap<String, List<Player>>();
	HashMap<Player, Player> compassTrackingMap = new HashMap<Player, Player>();
	String gametype;
	UpdateTrackingCompass timer;
	Lobby lobby;
	Location respawnLocation;
	Location endGameLocation;
	Boolean gameInProgress;
	public static List<Player> preventFallDamagePlayers = new ArrayList<Player>();
	
	/*
	 * Used in StartGame class to initialize teams
	 */
	public void initializeTeamsManager(HashMap<String, List<Player>> teams, Location respawnLocation, Location endGameLocation, String gametype, Lobby lobby) {
		this.teams.putAll(teams);
		this.gametype = gametype;
		this.lobby = lobby;
		this.respawnLocation = respawnLocation;
		this.endGameLocation = endGameLocation;
		this.gameInProgress = true;
		timer = new UpdateTrackingCompass((plugin.getConfig().getInt("gameconfig.timer") * 60), this);
		Player firstSurvivalist = teams.get("Survivalist").get(0);
		for(Player player : this.getAllPlayersInLobby()) {
			compassTrackingMap.put(player, firstSurvivalist);
			preventFallDamagePlayers.add(player);
		}
	}
	
	/*
	 * Returns list of players who won't take fall damage
	 */
	public List<Player> getNoFallDamagePlayers(){
		return preventFallDamagePlayers;
	}
	
	/*
	 * Returns the original lobby (same instance) from MinigamesLobby plugin. Needed to clearTeams at the end
	 */
	public Lobby getSurvivalistLobby() {
		return lobby;
	}
	
	/*
	 * Takes player, removes them from survivalist and adds to hunter list
	 */
	public void moveToHunters(Player player) {
		if(teams.get("Survivalist").contains(player)) {
			teams.get("Survivalist").remove(player);
			teams.get("Hunter").add(player);
		}
	}
	
	/*
	 * Returns the teams hashmap
	 */
	public HashMap<String, List<Player>> getAllTeams(){
		return teams;
	}
	
	/*
	 * Returns the compass tracking map
	 */
	public HashMap<Player, Player> getTrackingMap(){
		return compassTrackingMap;
	}
	
	/*
	 * Gets the countdownTimer for this lobby
	 */
	public UpdateTrackingCompass getTimer() {
		return timer;
	}
	/*
	 * Get all players in lobby
	 */
	public List<Player> getAllPlayersInLobby(){
		List<Player> allPlayers = new ArrayList<Player>();
		for(Map.Entry<String, List<Player>> entry : teams.entrySet()) {
			for(Player player : entry.getValue()) {
				allPlayers.add(player);
			}
		}
		return allPlayers;
	}
	
	/*
	 * Gets the gametype
	 */
	public String getGameType() {
		return gametype;
	}
	
	/*
	 * Gets the team of a Player. Else returns null
	 */
	public String getPlayerTeam(Player player) {
		for(Map.Entry<String, List<Player>> entry: teams.entrySet()) {
			for(Player testplayer : entry.getValue()) {
				if(testplayer.getName().equalsIgnoreCase(player.getName())) {
					return entry.getKey();
				}
			}
		}
		return null;
	}
	
	/*
	 * Adds a player to a team
	 */
	public void addPlayerToTeam(Player player, String team) {
		List<Player> newTeam = teams.get(team);
		newTeam.add(player);
		teams.put(team, newTeam);
	}
	
	/*
	 * Removes a player from a team
	 */
	public void removePlayerFromTeam(Player player, String team) {
		List<Player> newTeam = teams.get(team);
		newTeam.remove(player);
		teams.put(team, newTeam);
	}
	
    /*
     * Called when a survivalist leaves/dies. Gets a player as input and if someone is tracking that player, makes someone track someone else.
     */
    public void changeTrackingMap(Player player) {
    	for(Map.Entry<Player, Player> entry : getTrackingMap().entrySet()) {
    		if(entry.getValue().equals(player)) {
    			//Get the team of the player so that you can get the opposite team
    			String team = getPlayerTeam(entry.getValue());
    			if(team.equalsIgnoreCase("Survivalist")) {
    				getTrackingMap().put(entry.getKey(), getAllTeams().get("Hunter").get(0));
    			}
    			else {
    				getTrackingMap().put(entry.getKey(), getAllTeams().get("Survivalist").get(0));
    			}
    		}
    	}
    }
    /*
     * Returns the RespawnLocation
     */
    public Location getRespawnLocation() {
    	return respawnLocation;
    }
    
    /*
     * Returns the EndGameLocation
     */
    public Location getEndGameLocation() {
    	return this.endGameLocation;
    }
    
    /*
     * Returns if game is in-progress or not
     */
    public Boolean gameInProgress() {
    	return gameInProgress;
    }
    
    /*
     * Modifies gameInProgress
     */
    public void setGameInProgress(Boolean bool) {
    	gameInProgress = bool;
    }
}
