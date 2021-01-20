package xyz.ravencraft.RCSurvivalist.Kits;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import xyz.ravencraft.RCSurvivalist.Main;


public class KitEditorGUI {
	
	private Main plugin = Main.getPlugin(Main.class);
	KitFileManager kitMang = KitFileManager.getKitMang();

	public Inventory createKitGUI(String kitname, int level) {
		
		Inventory inv = Bukkit.createInventory(null, 36, ChatColor.translateAlternateColorCodes('&', "&5RCS Kit Editor - &9" + kitname + " "
				+ String.valueOf(level)));
		return inv;
		
	}
	
	public Inventory createKitEditorGUI(String kitname, int level, ItemStack[] contents) {
		
		//Create inventory (size 36) with those contents
		Inventory inv = Bukkit.createInventory(null, 36, ChatColor.translateAlternateColorCodes('&', "&5RCS Kit Editor - &9" + kitname + " "
				+ String.valueOf(level)));
		inv.setContents(contents);
		return inv;		
	}
	
}
