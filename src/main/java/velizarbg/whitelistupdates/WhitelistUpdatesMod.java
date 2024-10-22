package velizarbg.whitelistupdates;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhitelistUpdatesMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("whitelistupdates");
	public static ModConfig config;

	@Override
	public void onInitialize() {
		config = ModConfig.load();
	}
}
