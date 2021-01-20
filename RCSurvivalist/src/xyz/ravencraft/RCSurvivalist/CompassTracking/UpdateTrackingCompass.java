package xyz.ravencraft.RCSurvivalist.CompassTracking;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import VaultIntegration.VaultIntegration;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import xyz.ravencraft.MinigamesLobby.EndGameEvent;
import xyz.ravencraft.RCSurvivalist.LangManager;
import xyz.ravencraft.RCSurvivalist.Main;
import xyz.ravencraft.RCSurvivalist.TeamsManager;
import xyz.ravencraft.RCSurvivalist.PlayerData.PlayerInfo;

public class UpdateTrackingCompass implements Runnable {

	int secondsLeft;
	int seconds;
	private Integer assignedTaskId;
	private Main plugin = Main.getPlugin(Main.class);
	LangManager lmang = LangManager.getLmang();
	TeamsManager teamMang;
	PlayerInfo playerInfo = PlayerInfo.getPlayerInfo();
	List<Double> broadcastTimes = Arrays.asList(20.0, 15.0, 10.0, 5.0, 3.0, 1.0);
	VaultIntegration vaultInt = VaultIntegration.getVaultIntegration();
	Economy eco = vaultInt.getEconomy();
	
	public UpdateTrackingCompass(int seconds, TeamsManager teamMang) {
		this.seconds = seconds;
		this.secondsLeft = seconds;
		this.teamMang = teamMang;
	}
	
	@Override
	public void run() {
        // Is the timer up?
        if (secondsLeft < 1) {
            // Do what was supposed to happen after the timer
        	
            secondsLeft = seconds;

            // Cancel timer
            if (assignedTaskId != null) Bukkit.getScheduler().cancelTask(assignedTaskId);
            
            //End the game
			for(Player TBRemoved : teamMang.getAllPlayersInLobby()) {
				if(playerInfo.isInSurvivalistGame(TBRemoved)) {
					playerInfo.getPlayersInAllGames().remove(TBRemoved);
					TBRemoved.setBedSpawnLocation(null);
					TBRemoved.getInventory().clear();
					TBRemoved.setHealth(20);
					TBRemoved.setFoodLevel(20);
					eco.depositPlayer(TBRemoved, 100.0);
//					if(TBRemoved.equals(player)) TBRemoved.spigot().respawn();
					TBRemoved.teleport(teamMang.getEndGameLocation());
					TBRemoved.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				lmang.getLang().getString("Survivalists-win")));
					Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Removed " + TBRemoved.getName() + " from game!");
				}
			}
			//Call End Game event
			Bukkit.getPluginManager().callEvent(new EndGameEvent(teamMang.getSurvivalistLobby()));
			teamMang.getTimer().stopTimer();
			return;
            
        }
        //Broadcast warning to all players in lobby at 20 minutes, 15 minutes, 10 minutes, 5 minutes, 3 minutes, 1 minute.
        for(double minutes : broadcastTimes) {
        	if(secondsLeft == (60 * minutes)) {
        		//broadcast to all players about time remaining
        		for(Player player : teamMang.getAllPlayersInLobby()){
        			player.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " &aThe game will"
        					+ " end in &2" + (secondsLeft / 60) + " &aminutes!"));
        		}
        	}
        }
        
        
        //Update the compass for hunter team
        for(Player player : teamMang.getAllPlayersInLobby()) {
        	if(!teamMang.getPlayerTeam(player).equalsIgnoreCase("Survivalist")) {
        		this.updateCompass(player);
        	}
        }


        // Decrement the seconds left
        secondsLeft--;
		
	}
	
    /**
     * Gets the seconds left this timer should run
     *
     * @return Seconds left timer should run
     */
    public int getSecondsLeft() {
        return secondsLeft;
    }
    
    /*
     * Adds time to the clock (used to add time when survivalists die)
     */
    public void addSecondsLeft(int addedSeconds) {
    	secondsLeft = secondsLeft + addedSeconds;
    	
    	//Broadcast to players time has been added
    	for(Player player : teamMang.getAllPlayersInLobby()) {
    		player.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " 
    	+ lmang.getLang().getString("Time-added")));
    	}
    }

    /**
     * Schedules this instance to "run" every second
     */
    public void scheduleTimer() {
        // Initialize our assigned task's id, for later use so we can cancel
        this.assignedTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 20L);
    }
    
    /*
     * Stops the timer
     */
    public void stopTimer() {
    	Bukkit.getScheduler().cancelTask(this.assignedTaskId);
    	secondsLeft = seconds;
    }
    
    /*
     * Update the compass
     */
    public void updateCompass(Player player) {
    Player toBeTracked = teamMang.getTrackingMap().get(player);
    if(toBeTracked == null) {
    	String team = teamMang.getPlayerTeam(player);
		if(team.equalsIgnoreCase("Survivalist")) {
			teamMang.getTrackingMap().put(player, teamMang.getAllTeams().get("Hunter").get(0));
			toBeTracked = teamMang.getAllTeams().get("Hunter").get(0);
		}
		else {
			teamMang.getTrackingMap().put(player, teamMang.getAllTeams().get("Survivalist").get(0));
			toBeTracked = teamMang.getAllTeams().get("Survivalist").get(0);
		}
    }
    player.setCompassTarget(toBeTracked.getLocation());
    
    //Use Pythag Theorem to show distance away in action bar
    int distance = this.getDistance(player, toBeTracked);
    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.translateAlternateColorCodes('&', 
    		"&aTracking player &2" + toBeTracked.getName() + "&a - &2" + distance + "m &aaway")));
    }
    
    /*
     * Gets distance between 2 players
     */
    public int getDistance(Player player1, Player player2) {
    	int x1 = player1.getLocation().getBlockX();
    	int x2 = player2.getLocation().getBlockX();
    	int z1 = player1.getLocation().getBlockZ();
    	int z2 = player2.getLocation().getBlockZ();
    	
    	int distance = (int) Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((z2 - z1), 2));
    	return distance;
    }
    


}
