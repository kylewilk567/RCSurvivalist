package xyz.ravencraft.MySQL;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

public class SQLEditor {

	Main plugin = Main.getPlugin(Main.class);
	SQLSetup SQL = SQLSetup.getSetup();
	static SQLEditor Editor;
	KitFileManager kitMang = KitFileManager.getKitMang();
	
	/*
	 * Returns instance of this class used in this plugin
	 */
	public static SQLEditor getEditor() {
		if(Editor == null) {
			Editor = new SQLEditor();
		}
		return Editor;
	}
	
	/*
	 * Creates the string needed to initialize table for kits
	 */
	public String createKitStringForTable() {
		String returnedString = "";
		for(String s : kitMang.getAllKits()) {
			returnedString = returnedString + s.toUpperCase() + " VARCHAR(100),";
		}
		
		return returnedString;
	}
/*
 * Create table if it does not already exist
 */
	public void createTable() {
		PreparedStatement ps; //Be sure to use JAVA Sql imports
		try {
			ps = SQL.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS " + plugin.getConfig().getString("datastorage.tableprefix") + "playerdata" +   
		" (NAME VARCHAR(100),UUID VARCHAR(100),LOGIN BIGINT," + createKitStringForTable() + "PRIMARY KEY (UUID))");
			ps.executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Returns T/F if player is in table already or not
	 */
	public boolean playerExists(UUID uuid) {
		try {
			//Select EVERYTHING (*) from table where UUID is equal
			PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT * FROM " + plugin.getConfig().getString("datastorage.tableprefix")
					+ "playerdata" + " WHERE UUID=?");
			ps.setString(1, uuid.toString());
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
	public void createPlayer(Player player) {
		try {
			UUID uuid = player.getUniqueId();
			if(!playerExists(uuid)) {
				PreparedStatement ps2 = SQL.getConnection().prepareStatement("INSERT IGNORE INTO " + plugin.getConfig().getString("datastorage.tableprefix") + "playerdata" +  
			"(NAME,UUID,LOGIN) VALUES (?,?,?)");
				ps2.setString(1, player.getName());
				ps2.setString(2, uuid.toString());
				ps2.setLong(3, System.currentTimeMillis());
				ps2.executeUpdate();
				return;
			}

		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Updates a player's last login time
	 */
	public void updatePlayerLastLogin(UUID uuid) {
		try {
			
				PreparedStatement ps2 = SQL.getConnection().prepareStatement("UPDATE " + plugin.getConfig().getString("datastorage.tableprefix") + "playerdata" +  
			" SET LOGIN=? WHERE UUID=?");
				ps2.setLong(1, System.currentTimeMillis());
				ps2.setString(2, uuid.toString());
				ps2.executeUpdate();
				return;
			

		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Retrieves a player's kit level. Returns 1 if not found
	 */
	public int getPlayerKitLevel(UUID uuid, String kitname) {
		try {
			PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT " + kitname.toUpperCase() + " FROM " +
		plugin.getConfig().getString("datastorage.tableprefix") + "playerdata" +  " WHERE UUID=?");
			ps.setString(1, uuid.toString());
			ResultSet results = ps.executeQuery();
			int level = 1;
			if(results.next()) {
				level = results.getInt(kitname.toUpperCase());
				if(level == 0) level = 1;
				return level;
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return 1;
	}
	
	/*
	 * Set the player's kit level to a new value
	 */
	public void setPlayerKitLevel(UUID uuid, String kitname, int level) {
		try {
			if(playerExists(uuid)) {
				PreparedStatement ps2 = SQL.getConnection().prepareStatement("UPDATE " + plugin.getConfig().getString("datastorage.tableprefix") + "playerdata" +  
			" SET " + kitname.toUpperCase() + "=? WHERE UUID=?");
				ps2.setInt(1, level);
				ps2.setString(2, uuid.toString());
				ps2.executeUpdate();
				return;
			}

		} catch(SQLException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "ERROR! Unable to add kit level for the kit " + kitname + "! Does it exist in the database?");
		}
	}
	
	/*
	 * Used to import all current player data in folder to database. Doesn't do anything if player is already in database.
	 */
	public void importPlayerFolderData() {
		File file = new File(plugin.getDataFolder().getPath() + File.separator + "PlayerData");
		File[] allPlayerFiles = file.listFiles();
		for(File f : allPlayerFiles) {
			YamlConfiguration fConfig = YamlConfiguration.loadConfiguration(f);
			//Create the player
			Long login = fConfig.getLong("LastLogin");
			String uuid = f.toString().substring(f.toString().length() - 40, f.toString().length()-4);
			try {
			if(!playerExists(UUID.fromString(uuid))) {
				PreparedStatement ps2 = SQL.getConnection().prepareStatement("INSERT IGNORE INTO " + plugin.getConfig().getString("datastorage.tableprefix") + "playerdata" +  
			"(UUID,LOGIN) VALUES (?,?)");
				ps2.setString(1, uuid.toString());
				ps2.setLong(2, login);
				ps2.executeUpdate();
				
				//Set the player's kit levels IF THEY EXIST
				fConfig.getConfigurationSection("kits").getKeys(false).forEach(key ->{
					String kitname = key;
					int level = fConfig.getInt("kits." + key);
					this.setPlayerKitLevel(UUID.fromString(uuid), kitname, level);
				});
			}

		} catch(SQLException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Error! Could not add uuid " + uuid + " to database!");
			e.printStackTrace();
		}


		}
	}
	
	
	/*
	 * Used to rename a column for a kit in the database - essentially changing the name of a kit. ***NOT USED IN THIS PLUGIN 1/16/21
	 */
	public void changeKitName(String oldKitName, String newKitName) {
		try {
				PreparedStatement ps2 = SQL.getConnection().prepareStatement("ALTER TABLE " + plugin.getConfig().getString("datastorage.tableprefix") + "playerdata" +  
			" RENAME COLUMN " + oldKitName.toUpperCase() + " TO " + newKitName.toUpperCase());
				ps2.executeUpdate();
				return;
			

		} catch(SQLException e) {
			e.printStackTrace();
		}	
	}
	
	/*
	 * Used to add column to table when new kit is created
	 */
	public void createKitInTable(String kitname) {
		try {
			PreparedStatement ps2 = SQL.getConnection().prepareStatement("ALTER TABLE " + plugin.getConfig().getString("datastorage.tableprefix") + "playerdata" +  
		" ADD COLUMN " + kitname.toUpperCase() + " VARCHAR(100) AFTER LOGIN");
			ps2.executeUpdate();
			return;
		

	} catch(SQLException e) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Column already exists! Ignore this error.");
	}	
	}
	
	/*
	 * Purges old Playerdata in database if mysql enabled
	 */
	public void purgePlayerData() {
		try {
			PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT UUID, LOGIN FROM " + plugin.getConfig().getString("datastorage.tableprefix")
					+ "playerdata");
			ResultSet results = ps.executeQuery();
			while(results.next()) {
	
				Long LastLogin = results.getLong("LOGIN");
				double daysOff = (double) ((System.currentTimeMillis() - LastLogin) / 86400000);
				if(plugin.getConfig().getDouble("playerdata.purge-time") < daysOff) {
					PreparedStatement ps2 = SQL.getConnection().prepareStatement("DELETE FROM " + plugin.getConfig().getString("datastorage.tableprefix")
							+ "playerdata" + " WHERE UUID=?");
					ps2.setString(1, results.getString("UUID"));
					ps2.executeUpdate();
				}
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}


	}
	
	
}
