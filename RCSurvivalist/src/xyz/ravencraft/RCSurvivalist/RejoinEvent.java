package xyz.ravencraft.RCSurvivalist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import xyz.ravencraft.RCSurvivalist.Kits.KitFileManager;
import xyz.ravencraft.RCSurvivalist.Modes.Casual;
import xyz.ravencraft.RCSurvivalist.PlayerData.PlayerDataManager;
import xyz.ravencraft.RCSurvivalist.PlayerData.PlayerInfo;


public class RejoinEvent implements Listener {
	
	PlayerInfo playerInfo = PlayerInfo.getPlayerInfo();
	LangManager lmang = LangManager.getLmang();
	Main plugin = Main.getPlugin(Main.class);
	LobbyManager lobbyMang = LobbyManager.getLobbyMang();
	Set<String> rejoinList = new HashSet<String>();

	public static Inventory createRejoinGUI() {
		Inventory inv = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&5Rejoin Survivalist Game?"));
		//Add join yes/no options
		ItemStack item = new ItemStack(Material.EMERALD_BLOCK, 1);
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<String>();
		meta.setDisplayName(ChatColor.GREEN + "Yes");
		lore.add(" ");
		lore.add(ChatColor.DARK_GRAY + "Click to rejoin game");
		meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(11, item);
		item.setType(Material.REDSTONE_BLOCK);
		meta.setDisplayName(ChatColor.RED + "No");
		lore.clear();
		meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(15, item);
		
		//Fill rest with stained glass panes
		while(inv.firstEmpty() != -1) {
			inv.setItem(inv.firstEmpty(), new ItemStack(Material.PURPLE_STAINED_GLASS_PANE, 1));
		}
		
		
		return inv;
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(event.getWhoClicked().getType() != EntityType.PLAYER) return;
		if(playerInfo.isInSurvivalistGame((Player) event.getWhoClicked())) {
			Player player = (Player) event.getWhoClicked();
			//Check if it's the rejoin inventory
			String title = ChatColor.stripColor(event.getView().getTitle());
			if(title.contains("Rejoin Survivalist Game?")) {
				
				if(event.getCurrentItem() == null) return;
				if(event.getCurrentItem().getItemMeta() == null) {
					event.setCancelled(true);
					return;
				}
				if(event.getCurrentItem().getItemMeta().getDisplayName() == null) {
					event.setCancelled(true);
					return;
				}
				if(event.getCurrentItem().getItemMeta().getDisplayName().contains("No")) {
					Player toBeRemoved = null;
					for(Player playerInList : playerInfo.getPlayersInAllGames()) {
						if(playerInList.getUniqueId().toString().equalsIgnoreCase(player.getUniqueId().toString())) 
							toBeRemoved = playerInList;	
					}
					if(toBeRemoved != null) playerInfo.getPlayersInAllGames().remove(toBeRemoved);
					rejoinList.remove(player.getUniqueId().toString());
					player.closeInventory();
					return;
				}
				if(event.getCurrentItem().getItemMeta().getDisplayName().contains("Yes")) {
					//Add them to hunter team
					playerInfo.getLobbyFromPlayer(player).addPlayerToTeam(player, "Hunter");
					//Teleport them back to respawn area and give their kit (only give kit for Casual mode)
					player.teleport(playerInfo.getLobbyFromPlayer(player).getRespawnLocation());
					rejoinList.remove(player.getUniqueId().toString());
					if(!playerInfo.getLobbyFromPlayer(player).getGameType().equalsIgnoreCase("Casual")) return;
					
					//Give them their kit back
					String kitName = PlayerInfo.getPlayerInfo().getPlayerKit(player);
					//Get kitlevel from playerDataFile
					int level = PlayerDataManager.getPlayerFile(plugin, player.getUniqueId().toString()).getInt("kits." + kitName);
					@SuppressWarnings("unchecked")
					ItemStack[] contents = ((List<ItemStack>) KitFileManager.getKitFile(plugin, kitName).get(Integer.toString(level))).toArray(new ItemStack[0]);
					for(ItemStack item : contents) {
						if(item == null) continue;
						player.getInventory().addItem(item);
					}
					Casual.getCooldowns().put(player, System.currentTimeMillis() + (1000 * 120));
					return;
				}
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void rejoinGUIOpen(InventoryOpenEvent event) {
		if(!playerInfo.isInSurvivalistGame((Player) event.getPlayer())) return;
		
		Player player = (Player) event.getPlayer();
		//Check if it's the rejoin inventory
		String title = ChatColor.stripColor(event.getView().getTitle());
		if(title.contains("Rejoin Survivalist Game?")) {
			rejoinList.add(player.getUniqueId().toString());
		}
	}
	
	@EventHandler
	public void rejoinGUIClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		//Check if it's the rejoin inventory
		String title = ChatColor.stripColor(event.getView().getTitle());
		if(title.contains("Rejoin Survivalist Game?")) {
			new BukkitRunnable() {

				@Override
				public void run() {
					if(rejoinList.contains(player.getUniqueId().toString())) player.openInventory(createRejoinGUI());	
				}
				
			}.runTaskLater(plugin, 20L);

		}
		return;
	}
	
}
