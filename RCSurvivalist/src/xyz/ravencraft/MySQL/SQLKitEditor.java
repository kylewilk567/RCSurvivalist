package xyz.ravencraft.MySQL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import xyz.ravencraft.RCSurvivalist.Main;
import xyz.ravencraft.RCSurvivalist.Kits.KitFileManager;

public class SQLKitEditor {

	Main plugin = Main.getPlugin(Main.class);
	SQLSetup SQL = SQLSetup.getSetup();
	static SQLKitEditor Editor;
	KitFileManager kitMang = KitFileManager.getKitMang();
	
	/*
	 * Returns instance of this class used in this plugin
	 */
	public static SQLKitEditor getEditor() {
		if(Editor == null) {
			Editor = new SQLKitEditor();
		}
		return Editor;
	}
	
/*
 * Create table if it does not already exist
 */
	public void createTable() {
		PreparedStatement ps; //Be sure to use JAVA Sql imports
		try {
			ps = SQL.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS " + plugin.getConfig().getString("datastorage.tableprefix") + "kits" +  
		" (KITNAME VARCHAR(100),KIT TEXT,PRIMARY KEY (KITNAME))");
			ps.executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Returns T/F if player is in table already or not
	 */
	public boolean kitExists(String kitname, int level) {
		if(!SQL.isConnected()) return false;
		try {
			//Select EVERYTHING (*) from table where UUID is equal
			PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT KIT FROM " + plugin.getConfig().getString("datastorage.tableprefix") + "kits"
			+ " WHERE KITNAME=?");
			ps.setString(1, kitname.toUpperCase() + level);
			ResultSet results = ps.executeQuery();
			if(results.next()) {
				
				//player is found - return true
				return true;
			}
			return false;
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/*
	 * Creates player in data table if they do not exist
	 */
	public void createKit(String kitname, int level, String serializedString) {
		try {
			if(!kitExists(kitname.toUpperCase(), level)) {
				PreparedStatement ps2 = SQL.getConnection().prepareStatement("INSERT IGNORE INTO " + plugin.getConfig().getString("datastorage.tableprefix") + "kits"
			+ "(KITNAME,KIT) VALUES (?,?)");
				ps2.setString(1, kitname.toUpperCase() + level);
				ps2.setString(2, serializedString);
				ps2.executeUpdate();
				return;
			}

		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	/*
	 * Retrieves a player's kit level. Returns 1 if not found
	 */
	public String getKit(String kitname, int level) {
		try {
			PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT " + "KIT" + " FROM " +
					plugin.getConfig().getString("datastorage.tableprefix") + "kits" + " WHERE KITNAME=?");
			ps.setString(1, kitname.toUpperCase() + level);
			ResultSet results = ps.executeQuery();
			if(results.next()) {
				String kit = results.getString("KIT");
				return kit;
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * Set the player's kit level to a new value
	 */
	public void setKit(String kitname, int level, String serializedConfig) {
		try {
			if(kitExists(kitname, level)) {
				PreparedStatement ps2 = SQL.getConnection().prepareStatement("UPDATE " + plugin.getConfig().getString("datastorage.tableprefix") + "kits" +  
			" SET " + "KIT" + "=? WHERE KITNAME=?");
				ps2.setString(1, serializedConfig);
				ps2.setString(2, kitname.toUpperCase() + level);
				ps2.executeUpdate();
				return;
			}

		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Used to import all current kit data in folder to database
	 */
	public Boolean importKitFolderData() {
		List<String> kits = kitMang.getAllKits();
		if(kits == null) {
			return false;
		}
		for(String kit : kits) {
			int level = 1;
			int slot = 0;
			YamlConfiguration config = new YamlConfiguration();
			while(kitMang.isKitLevel(kit, level)) { //Checks if the kit has another level in the FOLDER
			//Checks if the kit and level are already stores in the database
			if(this.kitExists(kit, level)) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "The following kit already exists and was not imported!"
			+ ChatColor.YELLOW + " " + kit + level);
				++level;
				continue;
			}
			@SuppressWarnings("unchecked")
			ItemStack[] contents = ((List<ItemStack>) KitFileManager.getKitFile(plugin, kit).get(Integer.toString(level))).toArray(new ItemStack[0]);
			for(ItemStack item : contents) {
				config.set(Integer.toString(slot), item);
				++slot;
			}
			String serializedConfig = config.saveToString();
			this.createKit(kit, level, serializedConfig);
			++level;
			}
		}
		return true;
	}
	
	
	
}
