package xyz.ravencraft.RCSurvivalist.AutoRegen;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;

import xyz.ravencraft.MinigamesLobby.PostEndGameEvent;
import xyz.ravencraft.RCSurvivalist.Main;
import xyz.ravencraft.RCSurvivalist.TeamsManager;

public class RegenWorldTimer implements Runnable {

	int assignedTaskId;
	Main plugin = Main.getPlugin(Main.class);
	List<Location> locations;
	TeamsManager teamMang;
	
	public RegenWorldTimer(List<Location> locations, TeamsManager teamMang) {
		this.locations = locations;
		this.teamMang = teamMang;
	}
	
	@Override
	public void run() {
		//If no more location to regen, stop the timer
		if(locations.size() == 0) {
			Bukkit.getScheduler().cancelTask(this.assignedTaskId);
			RegenData.RegenManager.get(teamMang).clear();
			Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Regen completed!");
			teamMang.setGameInProgress(false);
			Bukkit.getPluginManager().callEvent(new PostEndGameEvent(teamMang.getSurvivalistLobby()));
			return;
		}
		
		Location location = locations.get(0);
		
		//Regen the first location in the list
		int blockChunkX = location.getBlockX();
		int blockChunkZ = location.getBlockZ();
		int blockChunkY = location.getBlockY();
		World world = BukkitAdapter.adapt(location.getWorld());
		
		//Obtain the corners of the chunk
		BlockVector3 vector1 = BlockVector3.at(blockChunkX -7, blockChunkY - 7, blockChunkZ -7);
		BlockVector3 vector2 = BlockVector3.at(blockChunkX + 7, blockChunkY + 7, blockChunkZ + 7);
		
		//Make chunk into a region **not sure how to do
		CuboidRegion region = new CuboidRegion(world, vector1, vector2);
		
		
		try(EditSession session = WorldEdit.getInstance().newEditSession(world)){
			if(world.regenerate(region, session)) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Chunk successfully regenerated!");
				locations.remove(location);
			}
		}
		
	}
	
	
	public void scheduleTimer() {
		this.assignedTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 500L);
	}

}
