package xyz.ravencraft.RCSurvivalist;

import java.util.HashMap;

public class LobbyManager {
	
	HashMap<String, TeamsManager> lobbies = new HashMap<String, TeamsManager>();
	private static LobbyManager lobbyMang;
	
	/*
	 * Returns the instance of LobbyManager used in this plugin
	 */
	public static LobbyManager getLobbyMang() {
			if(lobbyMang == null) {
				lobbyMang = new LobbyManager();
			}
			return lobbyMang;
		}
	
	/*
	 * Adds a new lobby to the Hashmap upon the start of a new game (or replaces if same lobbyname)
	 */
	public void addLobby(String lobbyname, TeamsManager lobby) {
		lobbies.put(lobbyname, lobby);
	}
	
	/*
	 * Returns the lobbies Map
	 */
	public HashMap<String, TeamsManager> getLobbies(){
		return lobbies;
	}

}
