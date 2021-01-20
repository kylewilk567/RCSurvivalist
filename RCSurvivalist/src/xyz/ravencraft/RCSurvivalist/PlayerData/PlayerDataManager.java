package xyz.ravencraft.RCSurvivalist.PlayerData;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.plugin.Plugin;

import xyz.ravencraft.RCSurvivalist.LangManager;
import xyz.ravencraft.RCSurvivalist.Main;
import xyz.ravencraft.RCSurvivalist.Kits.KitFileManager;

public class PlayerDataManager {

	private Main plugin = Main.getPlugin(Main.class);
	LangManager lmang = LangManager.getLmang();
	KitFileManager kitMang = KitFileManager.getKitMang();
	
	private static PlayerDataManager playerMang;
	
	/*
	 * gets the instance of playerdatamanager used in this plugin
	 */
	public static PlayerDataManager getPlayerMang() {
		if(playerMang == null) {
			playerMang = new PlayerDataManager();
		}
		return playerMang;
	}
	
	
	/*
	 * Creates playerdata FOLDER
	 */
	public void setupPlayerDataFolder() {
		File file = new File(plugin.getDataFolder().getPath() + File.separator + "PlayerData");
		
		if(!file.exists()) {
		file.mkdirs();
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + 
				" " + lmang.getLang().getString("PlayerDatafolder-created")));
		return;
		}
		return;
	}
	
	/*
	 * Create the player FILE. Return false if file already exists or error
	 */
	public Boolean createPlayerFile(String UUID) {
		File file = new File(plugin.getDataFolder().getPath() + File.separator + "PlayerData" + File.separator + UUID + ".yml");
		
		if(!file.exists()) {
		try {
		file.createNewFile();
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + 
				" " + lmang.getLang().getString("PlayerFile-created")));
		YamlConfiguration pConfig = YamlConfiguration.loadConfiguration(file);
		for(String kit : kitMang.getAllKits()) {
			pConfig.set("kits." + kit, 1);
		}
		pConfig.save(file);
		return true;
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}
		}
		return false;
	}
	
	/*
	 * Gets the file for a specific player. Returns null if it does not exist
	 */
	@Nullable
	public static YamlConfiguration getPlayerFile(Plugin plugin, String UUID) {
	   File f = new File(plugin.getDataFolder().getPath() + File.separator + "PlayerData" + File.separator + UUID + ".yml");
	    if (!f.exists()) {
	        return null;
	    }
	    return YamlConfiguration.loadConfiguration(f);
	}
	
	/*
	 * Purges old data according to config
	 */
	public void purgeOldPlayerData() {
		File file = new File(plugin.getDataFolder().getPath() + File.separator + "PlayerData");
		File[] allPlayerFiles = file.listFiles();
		for(File f : allPlayerFiles) {
			YamlConfiguration fConfig = YamlConfiguration.loadConfiguration(f);
			Long LastLogin = fConfig.getLong("LastLogin");
			double daysOff = (double) ((System.currentTimeMillis() - LastLogin) / 86400000);
			if(plugin.getConfig().getDouble("playerdata.purge-time") < daysOff) {
				f.delete();
			}
		}
	}
	
	
}
