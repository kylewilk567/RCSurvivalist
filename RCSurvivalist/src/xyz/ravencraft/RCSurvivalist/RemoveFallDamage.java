package xyz.ravencraft.RCSurvivalist;

import org.bukkit.GameRule;

public class RemoveFallDamage implements Runnable {
	
	TeamsManager teamMang;
	
	public RemoveFallDamage(TeamsManager teamMang) {
		this.teamMang = teamMang;
	}

	@Override
	public void run() {
		teamMang.getNoFallDamagePlayers().clear();
		teamMang.getAllPlayersInLobby().get(0).getWorld().setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
	}

}
