package VaultIntegration;

import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;
import xyz.ravencraft.RCSurvivalist.LangManager;
import xyz.ravencraft.RCSurvivalist.Main;

public class VaultIntegration {

	private Main plugin = Main.getPlugin(Main.class);
	public Economy eco;
	private static VaultIntegration vaultIntegration;

	/*
	 * Initial Economy setup
	 */
	public boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economy = 
				plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if(economy != null) {
			eco = economy.getProvider();
		}
		return (eco != null);
	}
	
	/*
	 * Get the economy
	 */
	public Economy getEconomy() {
		RegisteredServiceProvider<Economy> economy = 
				plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		eco = economy.getProvider();
		return eco;
	}
	
	/*
	 * Returns instance of VaultIntegration
	 */
	public static VaultIntegration getVaultIntegration() {
		if(vaultIntegration == null) {
			vaultIntegration = new VaultIntegration();
		}
		return vaultIntegration;
	}
	
}
