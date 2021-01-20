package xyz.ravencraft.RCSurvivalist.CompassTracking;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import xyz.ravencraft.RCSurvivalist.TeamsManager;


public class SelectorGUI {
	
	
	/*
	 * Returns the GUI used to track hunters/survivalists
	 */
	@SuppressWarnings("deprecation")
	public Inventory createTrackingGUI(Player player, TeamsManager teamMang) {
		//get the team the player is currently on. Then get all members of the opposite team
		String team = teamMang.getPlayerTeam(player);
		List<Player> toBeTracked = new ArrayList<Player>();
		if(team.equalsIgnoreCase("Survivalist")) {
			toBeTracked = teamMang.getAllTeams().get("Hunter");
		}
		else {
			toBeTracked = teamMang.getAllTeams().get("Survivalist");			
		}
		double num = toBeTracked.size() / 9;
		int size = (int) Math.round(num + .5);
		size = size * 9;
		Inventory inv = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', "&5&lR&9&lC&2&lS &cTracking Menu"));
		for(Player tracked : toBeTracked) {
			Material type = Material.PLAYER_HEAD;
			ItemStack item = new ItemStack(type, 1);
			SkullMeta meta = (SkullMeta) item.getItemMeta();
			List<String> lore = new ArrayList<String>();
			meta.setDisplayName(ChatColor.DARK_GREEN + tracked.getName());
			lore.add(ChatColor.RED + "Click to track");
			meta.setOwner(tracked.getName());
			item.setItemMeta(meta);
			inv.setItem(inv.firstEmpty(), item);
		}
		
		return inv;
	}
}
