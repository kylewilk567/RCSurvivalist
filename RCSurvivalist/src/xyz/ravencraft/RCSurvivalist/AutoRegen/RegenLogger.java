package xyz.ravencraft.RCSurvivalist.AutoRegen;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import xyz.ravencraft.RCSurvivalist.PlayerData.PlayerInfo;

public class RegenLogger implements Listener {
	
	PlayerInfo playerInfo = PlayerInfo.getPlayerInfo();
	RegenData regendata = RegenData.getRegenData();
	Material[] materialList = {Material.CHEST, Material.CHEST_MINECART, Material.ENDER_CHEST, Material.CRAFTING_TABLE, Material.FURNACE, Material.BLAST_FURNACE
			, Material.SMOKER, Material.HOPPER, Material.HOPPER_MINECART, Material.FURNACE_MINECART, Material.ENCHANTING_TABLE, Material.OBSIDIAN, Material.WHITE_BED};
	
	@EventHandler
	public void onCertainAction(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if(playerInfo.isInSurvivalistGame(player)) {
			if(playerInfo.getLobbyFromPlayer(player) != null) {
				for(Material material : materialList) {
					if(event.getBlock().getType() == material) {
						regendata.addLocationToMap(playerInfo.getLobbyFromPlayer(player), event.getBlock().getLocation());
					}
				}
			}

		}
	}

}
