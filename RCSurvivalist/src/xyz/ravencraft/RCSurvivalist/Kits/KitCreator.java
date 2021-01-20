package xyz.ravencraft.RCSurvivalist.Kits;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import xyz.ravencraft.MySQL.SQLKitEditor;
import xyz.ravencraft.RCSurvivalist.LangManager;
import xyz.ravencraft.RCSurvivalist.Main;

public class KitCreator implements Listener {
	
	Main plugin = Main.getPlugin(Main.class);
	LangManager lmang = new LangManager();
	SQLKitEditor kitEditor = SQLKitEditor.getEditor();

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		
		
		String title = ChatColor.stripColor(event.getView().getTitle());
		//Check if inventory matches one from this plugin
		if(title.contains("RCS Kit")) {
			
			//Get the kitname AND level from that GUI
			StringBuilder kitnameBuilder = new StringBuilder();
			int levelSlot = 0;
			for(int i = 17; i < title.length(); ++i) {
				char c = title.charAt(i);
				if(c != ' ') {
					kitnameBuilder.append(c);
					continue;
				}
				levelSlot = i + 1;
				break;
			}
			String kitname = ChatColor.stripColor(kitnameBuilder.toString());
			int level;
			try {
				level = Integer.parseInt(Character.toString(title.charAt(levelSlot)));
			} catch(Exception e) {
				e.printStackTrace();
				return;
			}
			
			// ------- For FOLDER datastorage type -------
			if(!plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL")) {
			YamlConfiguration kitFile = KitFileManager.getKitFile(plugin, kitname);
			if(kitFile == null) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
						lmang.getLang().getString("Error-null") + "Kit file of name " + kitname));
				return;
			}
			kitFile.set(String.valueOf(level), event.getInventory().getContents());
			
			//Save kitFile
			try {
				kitFile.save(new File(plugin.getDataFolder().getPath() + File.separator + "Kits" + File.separator + kitname + ".yml"));
				Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Kit file has been saved successfully.");
			} catch (IOException e) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Could not save Kit file "+ ChatColor.RED + kitname + ".yml");
				e.printStackTrace();
			}
		}
			
			// ----------- For MYSQL datastorage type --------------
			else {
				YamlConfiguration kitFile = new YamlConfiguration();
				int slot = 0;
				for(ItemStack item : event.getInventory().getContents()) {
					kitFile.set(Integer.toString(slot), item);
					++slot;
				}
				String serializedString = kitFile.saveToString();
				if(kitEditor.kitExists(kitname, level)) {
					kitEditor.setKit(kitname, level, serializedString);
					return;
				}
				kitEditor.createKit(kitname, level, serializedString);
				
			}
		}
	}
	
}
