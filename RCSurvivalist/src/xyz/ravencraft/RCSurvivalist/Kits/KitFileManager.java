package xyz.ravencraft.RCSurvivalist.Kits;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatColor;
import xyz.ravencraft.RCSurvivalist.LangManager;
import xyz.ravencraft.RCSurvivalist.Main;

public class KitFileManager {

	
	private Main plugin = Main.getPlugin(Main.class);
	LangManager lmang = LangManager.getLmang();
	
	private static KitFileManager kitMang;
	
	/*
	 * gets the instance of kitManager used in this plugin
	 */
	public static KitFileManager getKitMang() {
		if(kitMang == null) {
			kitMang = new KitFileManager();
		}
		return kitMang;
	}

	/*
	 * Creates kits FOLDER
	 */
	public void setupKitsFolder() {
		File file = new File(plugin.getDataFolder().getPath() + File.separator + "Kits");
		
		if(!file.exists()) {
		file.mkdirs();
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + 
				" " + lmang.getLang().getString("Kitfolder-created")));
		return;
		}
		return;
	}
	
	/*
	 * Create the kit FILE. Return false if file already exists or error
	 */
	public Boolean createKitFile(String kitname) {
		File file = new File(plugin.getDataFolder().getPath() + File.separator + "Kits" + File.separator + kitname + ".yml");
		
		if(!file.exists()) {
		try {
		file.createNewFile();
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + 
				" " + lmang.getLang().getString("KitFile-created")));
		return true;
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}
		}
		return false;
	}
	
	/*
	 * Gets the file for a specific kit. Returns null if it does not exist
	 */
	@Nullable
	public static YamlConfiguration getKitFile(Plugin plugin, String kitname) {
	   File f = new File(plugin.getDataFolder().getPath() + File.separator + "Kits" + File.separator + kitname + ".yml");
	    if (!f.exists()) {
	        return null;
	    }
	    return YamlConfiguration.loadConfiguration(f);
	}
	
	/*
	 * Save file for kit
	 */
	public void saveKit(String kitname) {
		File f = new File(plugin.getDataFolder().getPath() + File.separator + "Kits" + File.separator + kitname + ".yml");
		try {
			getKitFile(plugin, kitname).save(f);
			Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Kit file has been saved successfully.");
		} catch (IOException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Could not save Kit file.");
			e.printStackTrace();
		}
		}
	
	/*
	 * Returns the paths of all files in the kits folder. If none, returns null
	 */
	public List<String> getAllKits() {
		File file = new File(plugin.getDataFolder().getPath() + File.separator + "Kits");
		File[] allKits = file.listFiles();
		if(allKits.length < 1) {
			return null;
		}
		List<String> kitNames = new ArrayList<String>();
		for(File f : allKits) {
			String name = f.getPath();
			//Take the file path, start from the end, and modify the name to be the part between Kits/ and .yml
			for(int i = name.length() - 7; i < name.length() && i >= 0; --i) {
				if(name.substring(i, i+4).equalsIgnoreCase("Kits")){
					name = name.substring(i + 5, name.length() - 4);
					kitNames.add(name);
					break;
				}
			}
		}
		return kitNames;
	}
	
	/*
	 * Returns true if kit is already created, false if not. (Uses kits folder)
	 */
	public Boolean isKit(String kitname) {
		List<String> allKits = this.getAllKits();
		if(allKits == null) {
			return false;
		}
		for(String s : allKits) {
			if(kitname.equalsIgnoreCase(s)) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * Returns true if this kit already has this level. False if kit does not exist or level does not exist (Uses kits folder)
	 */
	public Boolean isKitLevel(String kitname, int kitlevel) {
		if(!isKit(kitname)) {
			return false;
		}
		
		YamlConfiguration KitFile = getKitFile(plugin, kitname);
		if(KitFile.getKeys(false).contains(String.valueOf(kitlevel))) {
			return true;
		}
			
		return false;
	}
}
