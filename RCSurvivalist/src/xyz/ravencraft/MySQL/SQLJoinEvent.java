package xyz.ravencraft.MySQL;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.md_5.bungee.api.ChatColor;
import xyz.ravencraft.RCSurvivalist.Main;
import xyz.ravencraft.RCSurvivalist.PlayerData.PlayerDataManager;

public class SQLJoinEvent implements Listener {

	Main plugin = Main.getPlugin(Main.class);
	SQLSetup SQL = SQLSetup.getSetup();
	SQLEditor Editor = SQLEditor.getEditor();
	SQLKitEditor kitEditor = SQLKitEditor.getEditor();
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if(!plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MySQL") && SQL.isConnected()) return;
		Player player = event.getPlayer();
		
		//Create player if they don't already exist
		if(!Editor.playerExists(player.getUniqueId())){
			YamlConfiguration playerFile = PlayerDataManager.getPlayerFile(plugin, player.getUniqueId().toString());
			//Player doesn't exist and no file --> Create player in database
			if(playerFile == null) {
				Editor.createPlayer(player);
				return;
			}
			
			//Player doesn't exist but has file --> Create player in database using file
			Editor.createPlayer(player);

			
			//Check if file should be deleted after adding to database
			if(plugin.getConfig().getBoolean("playerdata.purge-file-if-using-mysql")){
				File f = new File(plugin.getDataFolder().getPath() + File.separator + "PlayerData" + File.separator + player.getUniqueId() + ".yml");
				f.delete();
				}
		}
		
		
		//If they do exist, update their login time to current time
		Editor.updatePlayerLastLogin(player.getUniqueId());
		
		//TEMPORARY??? PERHAPS -- add the player's name to table if it is null (happens when playerdata is imported)
		try {
			PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT NAME FROM " + plugin.getConfig().getString("datastorage.tableprefix")
					+ "playerdata" + " WHERE UUID=?");
			ps.setString(1, player.getUniqueId().toString());
			ResultSet results = ps.executeQuery();
			results.next();
			if(results.getString("NAME") == null) {
				PreparedStatement ps2 = SQL.getConnection().prepareStatement("UPDATE " + plugin.getConfig().getString("datastorage.tableprefix") + "playerdata" +  
			" SET " + "NAME=? WHERE UUID=?");
				ps2.setString(1, player.getName());
				ps2.setString(2, player.getUniqueId().toString());
				ps2.executeUpdate();
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	
}
