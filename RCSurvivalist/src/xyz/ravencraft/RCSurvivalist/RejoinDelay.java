package xyz.ravencraft.RCSurvivalist;

import org.bukkit.entity.Player;

public class RejoinDelay implements Runnable {

	//THIS CLASS is used to allow player to rejoin a game. Used in PlayerJoinEvent in pluginevents();
	
	Player player;
	
	public RejoinDelay(Player player) {
		this.player = player;
	}
	
	@Override
	public void run() {
		player.openInventory(RejoinEvent.createRejoinGUI());
		
	}
	
}
