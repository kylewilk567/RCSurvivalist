package xyz.ravencraft.RCSurvivalist;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import VaultIntegration.VaultIntegration;
import xyz.ravencraft.MySQL.SQLEditor;
import xyz.ravencraft.MySQL.SQLJoinEvent;
import xyz.ravencraft.MySQL.SQLKitEditor;
import xyz.ravencraft.MySQL.SQLSetup;
import xyz.ravencraft.RCSurvivalist.AfkChecker.AfkChecker;
import xyz.ravencraft.RCSurvivalist.AutoRegen.RegenLogger;
import xyz.ravencraft.RCSurvivalist.CompassTracking.TrackingClickEvent;
import xyz.ravencraft.RCSurvivalist.Kits.KitCreator;
import xyz.ravencraft.RCSurvivalist.Kits.KitFileManager;
import xyz.ravencraft.RCSurvivalist.Modes.Casual;
import xyz.ravencraft.RCSurvivalist.PlayerData.PlayerDataManager;


public class Main extends JavaPlugin {
	
	AfkChecker afkChecker;
	private VaultIntegration econ;

	@Override
	public void onEnable() {

		//Set up Economy
		econ = new VaultIntegration();
		if(!(econ.setupEconomy())) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "You must have Vault installed and an economy plugin");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		//Check if PAPI is installed
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            new PAPIExpansion(this).register();
      } else {
    	  Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "PlaceholderAPI not found. Disabled RCSurvivalist PAPIExpansion.");
      }

		//Register command executors
		this.getCommand("rcs").setExecutor(new PluginCommands());
		
		//Register Events
		this.getServer().getPluginManager().registerEvents(new PluginEvents(), this);
		this.getServer().getPluginManager().registerEvents(new StartGame(), this);
		this.getServer().getPluginManager().registerEvents(new KitCreator(), this);
		this.getServer().getPluginManager().registerEvents(new TrackingClickEvent(), this);
		this.getServer().getPluginManager().registerEvents(new Casual(), this);
		this.getServer().getPluginManager().registerEvents(new RejoinEvent(), this);
		this.getServer().getPluginManager().registerEvents(new RegenLogger(), this);
		
		//Load files
		this.saveDefaultConfig();
		LangManager lmang = new LangManager();
		lmang.setupLang();
		KitFileManager kitMang = KitFileManager.getKitMang();
		kitMang.setupKitsFolder();
		
		if(!this.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL")) {
		PlayerDataManager playerMang = PlayerDataManager.getPlayerMang();
		playerMang.setupPlayerDataFolder();
		}
		else {
			SQLSetup SQL = SQLSetup.getSetup();
			SQLEditor Editor = SQLEditor.getEditor();
			SQLKitEditor kitEditor = SQLKitEditor.getEditor();
			try {
				SQL.connect();
			} catch (ClassNotFoundException | SQLException e) {
				//e.printStackTrace();
				Bukkit.getLogger().info("Database is not connected");
			}
			
			if(SQL.isConnected()) {
				Bukkit.getLogger().info("Database is connected!");
				Editor.createTable();
				kitEditor.createTable();
				this.getServer().getPluginManager().registerEvents((Listener) new SQLJoinEvent(), this);
			}
		}
		
		
		//Schedule afk timer
		afkChecker = new AfkChecker();
		afkChecker.scheduleTimer();
		
	}

	@Override
	public void onDisable() {
		if(this.getConfig().getBoolean("playerdata.purge-enabled")) {	
		if(!this.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL")) {
		PlayerDataManager playerMang = PlayerDataManager.getPlayerMang();
		playerMang.purgeOldPlayerData();
		}
		else {
			SQLEditor Editor = SQLEditor.getEditor();
			Editor.purgePlayerData();
		}
		}
		
		afkChecker.stopTimer();
	}

	
}
