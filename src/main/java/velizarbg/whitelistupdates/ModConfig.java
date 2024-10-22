package velizarbg.whitelistupdates;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import static velizarbg.whitelistupdates.WhitelistUpdatesMod.LOGGER;

public class ModConfig {
	public static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("whitelistupdates.json").toFile();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public String webhookId = "ENTER YOUR WEBHOOK ID HERE";
	public String webhookToken = "ENTER YOUR WEBHOOK TOKEN HERE";
	public String serverName = "ENTER YOUR SERVER NAME HERE";

	public static ModConfig load() {
		ModConfig config = null;
		if (CONFIG_FILE.exists()) {
			try (Reader fileReader = Files.newReader(CONFIG_FILE, StandardCharsets.UTF_8)) {
				config = GSON.fromJson(fileReader, ModConfig.class);
			} catch (IOException e) {
				LOGGER.error("Exception while loading config", e);
			}
		}
		// gson.fromJson() can return null if file is empty
		if (config == null) {
			config = new ModConfig();
		}
		config.save();
		return config;
	}

	public void save() {
		try (Writer writer = Files.newWriter(CONFIG_FILE, StandardCharsets.UTF_8)) {
			var jWriter = GSON.newJsonWriter(writer);
			jWriter.setIndent("\t");
			GSON.toJson(this, ModConfig.class, jWriter);
		} catch (IOException e) {
			LOGGER.error("Exception while saving config", e);
		}
	}
}