package xyz.ravencraft.RCSurvivalist;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;


public class LangManager {
	private Main plugin = Main.getPlugin(Main.class);
	private static LangManager lmang;

	
	public FileConfiguration langConfig;
	public File langFile;
	


	public void setupLang() {
		
	langFile = new File(plugin.getDataFolder(), "eng.yml");

	if(!langFile.exists()) {
			plugin.saveResource("eng.yml", false);
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', 
					getLang().getString("Prefix") + " &aeng.yml has been created."));
	}
	langConfig = YamlConfiguration.loadConfiguration(langFile);
}


public FileConfiguration getLang() {
	langFile = new File(plugin.getDataFolder(), plugin.getConfig().getString("language") + ".yml");
	langConfig = YamlConfiguration.loadConfiguration(langFile);
	return langConfig;
}

public void saveLang() {
	try {
		getLang().save(langFile);
		Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', 
				getLang().getString("Prefix") + " &aLanguage file has been saved."));
	} catch(IOException e ) {
		Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', 
				getLang().getString("Prefix") + " &cCould not save language file!"));
	}
}

public void reloadLang() {
	langFile = new File(plugin.getDataFolder(), plugin.getConfig().getString("language") + ".yml");
	langConfig = YamlConfiguration.loadConfiguration(langFile);
	Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', 
			getLang().getString("Prefix") + " &aLanguage has been reloaded."));
	return;
}

/*
 * Returns the instance of language used in this plugin
 */

public static LangManager getLmang() {
	if(lmang == null) {
		lmang = new LangManager();
	}
	return lmang;
}
}
