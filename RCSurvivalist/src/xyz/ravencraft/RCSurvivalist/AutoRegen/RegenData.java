package xyz.ravencraft.RCSurvivalist.AutoRegen;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;

import xyz.ravencraft.RCSurvivalist.Main;
import xyz.ravencraft.RCSurvivalist.TeamsManager;

public class RegenData {

	Main plugin = Main.getPlugin(Main.class);
	static HashMap<TeamsManager, List<Location>> RegenManager = new HashMap<TeamsManager, List<Location>>();
	private static RegenData regendata;
	
	/*
	 * Gets instance of this class used in this plugin
	 */
	public static RegenData getRegenData() {
		if(regendata == null) {
			regendata = new RegenData();
		}
		return regendata;
	}
	
	/*
	 * Gets the RegenManager map
	 */
	public HashMap<TeamsManager, List<Location>> getRegenManager(){
		return RegenManager;
	}
	
	/*
	 * Used to add a chunk to the HashMap - using this as I want to check if list already contains it
	 */
	public void addLocationToMap(TeamsManager teamMang, Location location) {
		if(RegenManager.get(teamMang).contains(location)) return;
		RegenManager.get(teamMang).add(location);
	}
	
	/*
	 * Regenerates the chunks in the specified TeamsManager
	 */
	public void regenerateChunks(TeamsManager teamMang) {
			RegenWorldTimer timer = new RegenWorldTimer(RegenManager.get(teamMang), teamMang);
			timer.scheduleTimer();
	}
}
