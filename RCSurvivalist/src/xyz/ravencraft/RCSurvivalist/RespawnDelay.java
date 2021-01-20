package xyz.ravencraft.RCSurvivalist;

import org.bukkit.entity.Player;

public class RespawnDelay implements Runnable {
	//THIS CLASS is used to respawn the survivalist who died last at the end game lobby
	
	TeamsManager teamMang;
	Player player;
	
	public RespawnDelay(TeamsManager teamMang, Player player) {
		this.teamMang = teamMang;
		this.player = player;
	}
	
	@Override
	public void run() {
		player.teleport(teamMang.getEndGameLocation());
		player.getInventory().clear();
		player.getEquipment().clear();
		
	}


	
}
