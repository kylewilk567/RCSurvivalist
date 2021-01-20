package xyz.ravencraft.RCSurvivalist.CompassTracking;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import xyz.ravencraft.RCSurvivalist.LobbyManager;
import xyz.ravencraft.RCSurvivalist.Main;
import xyz.ravencraft.RCSurvivalist.PlayerData.PlayerInfo;


public class TrackingClickEvent implements Listener {

	Main plugin = Main.getPlugin(Main.class);
	PlayerInfo playerInfo = PlayerInfo.getPlayerInfo();
	LobbyManager lobbyMang = LobbyManager.getLobbyMang();
	
	
	@EventHandler
	public void onTrackingClick(InventoryClickEvent event) {
		String title = ChatColor.stripColor(event.getView().getTitle());
		Player player = (Player) event.getWhoClicked();
		if(playerInfo.isInSurvivalistGame(player)) {
		if(title.contains("RCS Tracking Menu")) {
			if(event.getCurrentItem() == null) return;
			if(event.getCurrentItem().getItemMeta() == null) return;
			if(event.getCurrentItem().getItemMeta().getDisplayName() == null) return;
			
			//Get the item's display name and set the person to track that new person
			try {
				Player toBeTracked = Bukkit.getPlayer(ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()));
				lobbyMang.getLobbies().get(playerInfo.getPlayerLobbyMap().get(player.getUniqueId().toString())).getTrackingMap().put(player, toBeTracked);
				event.setCancelled(true);
				player.closeInventory();
				
			} catch(Exception e) {
				e.printStackTrace();
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Unable to add player to tracking map.");
			}
			
		}
		}
	}
	
}
