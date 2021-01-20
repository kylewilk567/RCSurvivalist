package xyz.ravencraft.RCSurvivalist;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import VaultIntegration.VaultIntegration;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import xyz.ravencraft.MySQL.SQLEditor;
import xyz.ravencraft.MySQL.SQLKitEditor;
import xyz.ravencraft.MySQL.SQLSetup;
import xyz.ravencraft.RCSurvivalist.AutoRegen.RegenData;
import xyz.ravencraft.RCSurvivalist.Kits.KitEditorGUI;
import xyz.ravencraft.RCSurvivalist.Kits.KitFileManager;
import xyz.ravencraft.RCSurvivalist.PlayerData.PlayerDataManager;
import xyz.ravencraft.RCSurvivalist.PlayerData.PlayerInfo;

public class PluginCommands implements CommandExecutor {
	
	Main plugin = Main.getPlugin(Main.class);
	LangManager lmang = new LangManager();
	KitFileManager kitMang = KitFileManager.getKitMang();
	KitEditorGUI kitGUI = new KitEditorGUI();
	PlayerDataManager playerMang = PlayerDataManager.getPlayerMang();
	LobbyManager lobbyMang = LobbyManager.getLobbyMang();
	PlayerInfo playerInfo = PlayerInfo.getPlayerInfo();
	RegenData regendata = RegenData.getRegenData();
	SQLEditor Editor = SQLEditor.getEditor();
	SQLKitEditor kitEditor = SQLKitEditor.getEditor();
	SQLSetup SQL = SQLSetup.getSetup();
	VaultIntegration vaultIntegration = VaultIntegration.getVaultIntegration();
	Economy eco = vaultIntegration.getEconomy();
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(label.equalsIgnoreCase("rcs")) {
			
			
			//If mySQL enabled and database is not connected, disable ALL commands!
			if(plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL") && !SQL.isConnected()) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				lmang.getLang().getString("MySQL-Not-Connected")));
				return true;
			}
			
		// ---------- /rcs AND /rcs help Player/Console -----------------------------
			if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
				//Check if console
				if(!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5&lR&9&lC&a&lSurvivalist &aby &bkwilk &aHelp Menu"
							+ "\n&bCurrently running v&3" + plugin.getDescription().getVersion() + 
							"\n&c/rcs help - &4displays this help menu.\n&c/rcs kit create <kitname> <level> - &4Opens GUI to insert items to create a kit."
							+ "\n&c/rcs kit edit <kitname> <level> - &4Opens GUI to modify the specified level of that kit.\n&c/rcs kit give <player>"
							+ " <kitname> <level> - &4gives player the level of the kit specified."
							+ "\n&c/rcs kit upgrade <player> <kitname> - &4" + 
							"Gives player the next level of the specified kit."));
					return true;
				}
				Player player = (Player) sender;
				if(!player.hasPermission("rcs.help")) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				lmang.getLang().getString("No-permission")));
					return true;
				}
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5&lR&9&lC&a&lSurvivalist &aby &bkwilk &aHelp Menu"
						+ "\n&bCurrently running v&3" + plugin.getDescription().getVersion() + 
						"\n&c/rcs help - &4displays this help menu.\n&c/rcs kit create <kitname> <level> - &4Opens GUI to insert items to create a kit."
						+ "\n&c/rcs kit edit <kitname> <level> - &4Opens GUI to modify the specified level of that kit.\n&c/rcs kit give <player>"
						+ " <kitname> <level> - &4gives player the level of the kit specified."
						+ "\n&c/rcs kit upgrade <player> <kitname> - &4" + 
						"Gives player the next level of the specified kit."));
				return true;
			}
			
			// ---------- /rcs test --------------------------------
			if(args[0].equalsIgnoreCase("test")) {
				Player player = (Player) sender;
				Editor.importPlayerFolderData();
				
				return true;
			}
			
		// IMPORTANT CHECK --> All commands listed after this point have at LEAST 2 arguments
			if(args.length < 2) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				lmang.getLang().getString("Use-help-cmd")));
				return true;
			}
			

			
		// ------------- /rcs kit create (kitname) (level) Player ----------------------------------
			if(args[0].equalsIgnoreCase("kit") && args[1].equalsIgnoreCase("create")) {
				
				//Check if console
				if(!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				lmang.getLang().getString("Player-only")));
					return true;
				}
				Player player = (Player) sender;
				//Check if player has perms
				if(!player.hasPermission("rcs.createkit")) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				lmang.getLang().getString("No-permission")));
					return true;
				}
				
				
				if(args.length == 4) {
				//Check integer argument IF one is specified
				try {
					Integer.parseInt(args[3]);
				} catch(Exception e) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				lmang.getLang().getString("NotAnInteger")));
					return true;
				}
				}
				
				//Set level argument - Set to 1 if no argument specified.
				int level = 1;
				if(args.length == 4) {
					level = Integer.parseInt(args[3]);
				}
				
				//Check if kitname AND level already exist. If so, return.
				if((kitMang.isKitLevel(args[2], level) && !plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL") ||
						(kitEditor.kitExists(args[2], level) && plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MySQL")))) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				lmang.getLang().getString("Error-KitandLevelExist")));
					return true;
				}
				
				//--Create the kit and level now!--
				
				if(!plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL"))
				kitMang.createKitFile(args[2]); //If file does not exist, create one
				
				// --- Add kit to mysql database for playerdata table if needed ---
				if(plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL")) {
					Editor.createKitInTable(args[2]);
				}
				
				//Open a GUI editor for player to create the kit and level - GUI created by KitEditorGUI class. The rest of this command is
				//handled by KitCreator class (on InventoryCloseEvent)
				player.openInventory(kitGUI.createKitGUI(args[2], level));
				return true;
			
			}
			
			// ------------ /rcs kit edit (kitname) (level) Player ---------------------
			if(args[0].equalsIgnoreCase("kit") && args[1].equalsIgnoreCase("edit")){
			
			//Check if console
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
			lmang.getLang().getString("Player-only")));
				return true;
			}
			
			Player player = (Player) sender;
			//Check if player has perms
			if(!player.hasPermission("rcs.editkit")) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
			lmang.getLang().getString("No-permission")));
				return true;
			}
			
			//Check all arguments are specified
			if(args.length < 4) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " &cUsage: /rcs kit edit "
						+ "<kitname> <level>"));
				return true;
			}
			int level = 1;
			//Check integer argument
			try {
				level = Integer.parseInt(args[3]);
			} catch(Exception e) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
			lmang.getLang().getString("NotAnInteger")));
				return true;
			}
			//Check kit AND level already created
			if(!kitMang.isKitLevel(args[2], level) && !plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL") || 
					!kitEditor.kitExists(args[2], level) && plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL")) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
			lmang.getLang().getString("Error-KitandLevelNotExistent")));
				return true;
			}
			
			//Checks complete! Get the items from their respective file, add to inventory, and open the editor.
			
			// --- IF FOLDER DATATYPE ---
			if(!plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL")) {
			@SuppressWarnings("unchecked")
			ItemStack[] contents = ((List<ItemStack>) KitFileManager.getKitFile(plugin, args[2]).get(args[3])).toArray(new ItemStack[0]);
			player.openInventory(kitGUI.createKitEditorGUI(args[2], level, contents));
			}

			// --- IF MYSQL DATATYPE ---
			else {
				YamlConfiguration loadedKit = new YamlConfiguration();
				String fromdatabase = kitEditor.getKit(args[2], level);
				List<ItemStack> contents = new ArrayList<ItemStack>();

				try {
					loadedKit.loadFromString(fromdatabase);
				} catch (InvalidConfigurationException e) {
					Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error in retrieving kit from RCS Database");
					e.printStackTrace();
				}	
				for(int slot = 0; slot < 36; ++slot) {
					ItemStack item = loadedKit.getItemStack(Integer.toString(slot));
				contents.add(item);
				}
				ItemStack[] newcontents = contents.toArray(new ItemStack[0]);
				player.openInventory(kitGUI.createKitEditorGUI(args[2], level, newcontents));

			}
			
			
			return true;
		}
			
			// ----- /rcs kit give (player) (kitname) (level)  Console/Player ---------------------
			if(args[0].equalsIgnoreCase("kit") && args[1].equalsIgnoreCase("give")) {
			
				//Check no perms
				if(sender instanceof Player) {
				Player player = (Player) sender;
				
				//Check if player has perms
				if(!player.hasPermission("rcs.givekit")) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				lmang.getLang().getString("No-permission")));
					return true;
				}
				}
				
				//Check player argument
				Player argument;
				try{
					argument = Bukkit.getPlayer(args[2]);
				} catch(Exception e) {
					e.printStackTrace();
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				lmang.getLang().getString("Error-PlayerNotFound")));
					return true;
				}
				
				//Check integer argument
				int level = 1;
				try {
					level = Integer.parseInt(args[4]);
				} catch(Exception e) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				lmang.getLang().getString("NotAnInteger")));
					return true;
				}
				//Check kitname and respective level exist
				if(!kitMang.isKitLevel(args[3], level) && !plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL") || 
						!kitEditor.kitExists(args[3], level) && plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				lmang.getLang().getString("Error-KitandLevelNotExistent")));
					return true;
				}
				
				//Checks complete! Get contents, remove the null ones, and give all to player argument
				
				// --- IF FOLDER DATATYPE ---
				if(!plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL")) {
				@SuppressWarnings("unchecked")
				ItemStack[] contents = ((List<ItemStack>) KitFileManager.getKitFile(plugin, args[3]).get(args[4])).toArray(new ItemStack[0]);
				
				for(ItemStack item : contents) {
					if(item == null) continue;
					argument.getInventory().addItem(item);
				}}
				
				// --- IF MYSQL DATATYPE ---
				else {
					YamlConfiguration loadedKit = new YamlConfiguration();
					String fromdatabase = kitEditor.getKit(args[3], level);
					List<ItemStack> contents = new ArrayList<ItemStack>();

					try {
						loadedKit.loadFromString(fromdatabase);
					} catch (InvalidConfigurationException e) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error in retrieving kit from RCS Database");
						e.printStackTrace();
					}	
					for(int slot = 0; slot < 36; ++slot) {
						ItemStack item = loadedKit.getItemStack(Integer.toString(slot));
					contents.add(item);
					}
					ItemStack[] newcontents = contents.toArray(new ItemStack[0]);
					for(ItemStack item : newcontents) {
						if(item == null) continue;
						argument.getInventory().addItem(item);
					}
				}
				return true;				
				
			}
			
			// ----------------- /rcs kit upgrade <player> <kitname> Player/Console -------------------------
			if(args[0].equalsIgnoreCase("kit") && args[1].equalsIgnoreCase("upgrade")) {
			//Check if player and has permission
			if(sender instanceof Player) {
				Player player = (Player) sender;
				if(!player.hasPermission("rcs.upgradekit")) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				lmang.getLang().getString("No-permission")));
					return true;
				}
			}
			
			//Check player argument (if player does not yet have a file, create one now OR if player not in database, add them now)
			try {
			Player argument = Bukkit.getPlayer(args[2]);
			if(PlayerDataManager.getPlayerFile(plugin, argument.getUniqueId().toString()) == null &&
					!plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL")) {
				playerMang.createPlayerFile(argument.getUniqueId().toString());
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
						lmang.getLang().getString("PlayerFile-created")));
			}
			if(plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL") && !Editor.playerExists(argument.getUniqueId())) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
						lmang.getLang().getString("Error-PlayerNotFound")));
				return true;
			}
			} catch(Exception e) {
				e.printStackTrace();
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				lmang.getLang().getString("Error-PlayerNotFound")));
				return true;
			}
			//Check kitname argument (does kit exist?)
			int level = 1;
			Player argument = Bukkit.getPlayer(args[2]);
			if(plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL")) {
				level = Editor.getPlayerKitLevel(argument.getUniqueId(), args[3]);
			}
			if(!kitMang.isKit(args[3]) && !plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL") || 
					!kitEditor.kitExists(args[3], level) && plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL")){
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				lmang.getLang().getString("Error-KitNotExistent")));
				return true;
			}
			//Check if kit has an upgrade

				//Get current kit level and add one --- FOLDER ONLY
			if(!plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL")) {
				YamlConfiguration playerFile = PlayerDataManager.getPlayerFile(plugin, argument.getUniqueId().toString());
			if(!kitMang.isKitLevel(args[3], (playerFile.getInt("kits." + args[3]) + 1))) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				lmang.getLang().getString("Error-KitandLevelNotExistent")));
				return true;
			}
			level = playerFile.getInt("kits." + args[3]);
			}
			
			// --- MYSQL Check if upgrade ---
			if(plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL")) {
				if(!kitEditor.kitExists(args[3], level + 1)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
							lmang.getLang().getString("Error-KitandLevelNotExistent")));
							return true;
				}
			}
			
			//Check if player has enough for the specified upgrade

			double cost = plugin.getConfig().getDouble("economy.kits.upgradecost." + Integer.toString(level + 1));
			if(eco.getBalance(argument) < cost) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				lmang.getLang().getString("Error-NotEnoughFunds")));
				return true;
			}
			
			//Get the kit and upgrade it one level - FOLDER
			if(!plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL")) {
				YamlConfiguration playerFile = PlayerDataManager.getPlayerFile(plugin, argument.getUniqueId().toString());
			playerFile.set("kits." + args[3], (level + 1));
			try {
				playerFile.save(new File(plugin.getDataFolder().getPath() + File.separator + "PlayerData" + File.separator + 
						argument.getUniqueId().toString() + ".yml"));
				//Withdraw the cost from their account
				eco.withdrawPlayer(argument, cost);
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
						lmang.getLang().getString("Kit-Upgraded") + argument.getName()));
			} catch (IOException e) {
				e.printStackTrace();
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				"&cError! Could not save playerdata for &4" + argument.getName()));
			}}
			//Get the kit and upgrade it one level - MYSQL
			if(plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL")) {
				Editor.setPlayerKitLevel(argument.getUniqueId(), args[3], level + 1);
				eco.withdrawPlayer(argument, cost);
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
						lmang.getLang().getString("Kit-Upgraded") + argument.getName()));
			}

			return true;
			}
			
			// --------------------------- /rcs kit import Player/Console --------------------------------------
			if(args[0].equalsIgnoreCase("kit") && args[1].equalsIgnoreCase("import")) {
				
				//Check if player and has permission
				if(sender instanceof Player) {
					Player player = (Player) sender;
					if(!player.hasPermission("rcs.importkit")) {
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
					lmang.getLang().getString("No-permission")));
						return true;
					}
				}
				
				//Check database connection
				if(!SQL.isConnected()) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
							lmang.getLang().getString("Database-Not-Connected")));
						return true;
				}
				
				//Import kits to database from folder
				if(kitEditor.importKitFolderData()) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
						lmang.getLang().getString("Kits-Imported")));
					return true;
				}
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
						lmang.getLang().getString("Error-Kits-NOT-Imported")));
				return true;
				
			}
			
			// --------------------------- /rcs playerdata import Player/Console ---------------------------
			if(args[0].equalsIgnoreCase("playerdata") && args[1].equalsIgnoreCase("import")) {
			//Check if player and has permission
			if(sender instanceof Player) {
				Player player = (Player) sender;
				if(!player.hasPermission("rcs.importdata")) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				lmang.getLang().getString("No-permission")));
					return true;
				}
			}
			//Check database connection
			if(!SQL.isConnected()) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
						lmang.getLang().getString("Database-Not-Connected")));
					return true;
			}
			
			//Import all player data to database from folder
			Editor.importPlayerFolderData();
			return true;
		}
			
		 //---------------------------- /rcs kit setlevel <player> <kitname> <level> Player/Console ------------------
			if(args[0].equalsIgnoreCase("kit") && args[1].equalsIgnoreCase("setlevel")) {
				//Check if player and has permission
				if(sender instanceof Player) {
					Player player = (Player) sender;
					if(!player.hasPermission("rcs.setlevelkit")) {
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
					lmang.getLang().getString("No-permission")));
						return true;
					}
				}
				
				//Check player argument (if player does not yet have a file, create one now OR if player not in database, add them now)
				try {
				Player argument = Bukkit.getPlayer(args[2]);
				if(PlayerDataManager.getPlayerFile(plugin, argument.getUniqueId().toString()) == null &&
						!plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL")) {
					playerMang.createPlayerFile(argument.getUniqueId().toString());
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
							lmang.getLang().getString("PlayerFile-created")));
				}
				if(plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL") && !Editor.playerExists(argument.getUniqueId())) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
							lmang.getLang().getString("Error-PlayerNotFound")));
					return true;
				}
				} catch(Exception e) {
					e.printStackTrace();
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
					lmang.getLang().getString("Error-PlayerNotFound")));
					return true;
				}
				
				//Check integer argument
				int level = 1;
				try {
					level = Integer.parseInt(args[4]);
				} catch(Exception e) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
				lmang.getLang().getString("NotAnInteger")));
					return true;
				}
				
				//Check kitname argument (does kit exist?)
				Player argument = Bukkit.getPlayer(args[2]);
				if(!kitMang.isKitLevel(args[3], level) && !plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL") || 
						!kitEditor.kitExists(args[3], level) && plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL")){
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
					lmang.getLang().getString("Error-KitNotExistent")));
					return true;
				}
				
				//Checks completed! Set the kit level for the player (argument)
				
				// --- FOLDER ---
				if(!plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL")) {
					YamlConfiguration playerConfig = PlayerDataManager.getPlayerFile(plugin, argument.getUniqueId().toString());
					playerConfig.set("kits." + args[3], level);
					try {
						playerConfig.save(new File(plugin.getDataFolder().getPath() + File.separator + "PlayerData" + File.separator + argument.getUniqueId() + ".yml"));
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
								lmang.getLang().getString("Kit-Level-Set")));
					} catch (IOException e) {
						e.printStackTrace();
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
						"&cError! Could not save playerdata for &4" + argument.getName()));
					}
				}
				
				// --- MYSQL ---
				else if(plugin.getConfig().getString("datastorage.type").equalsIgnoreCase("MYSQL")) {
					Editor.setPlayerKitLevel(argument.getUniqueId(), args[3], level);
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lmang.getLang().getString("Prefix") + " " + 
							lmang.getLang().getString("Kit-Level-Set")));
					return true;
				}
				
				
			}
			
			
		}
		
		return true;
	}

}
