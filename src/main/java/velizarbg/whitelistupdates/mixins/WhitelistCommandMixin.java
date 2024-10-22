package velizarbg.whitelistupdates.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.command.WhitelistCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.rcon.RconCommandOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import static velizarbg.whitelistupdates.WhitelistUpdatesMod.LOGGER;
import static velizarbg.whitelistupdates.WhitelistUpdatesMod.config;

@Mixin(WhitelistCommand.class)
public class WhitelistCommandMixin {
	@Unique
	private static HttpClient httpClient = null;

	@Unique
	private static void sendMessage(String message, ServerCommandSource source, GameProfile gameProfile) {
		if (httpClient == null) {
			httpClient = HttpClient.newBuilder().executor(source.getServer())/*.priority(256)*/.build();
		}

		var formattedMsg = message
			.formatted(
				config.serverName,
				// Using this instead of source.getEntity() because that is easily circumventable (e.g. /execute as) and gives less info
				switch(((ServerCommandSourceAccessor) source).getOutput()) {
					case ServerPlayerEntity player ->
						"[%s](<https://namemc.com/profile/%s>)".formatted(player.getNameForScoreboard(), player.getUuid());
					case MinecraftServer ignored -> "The console"; // This also happens when a data pack function (requires setting function-permission-level to 4) does it
					case RconCommandOutput ignored -> "RCON";
					case null, default -> {
						LOGGER.warn("Unknown output of command source");
						LOGGER.info("""
								Source output: {}
								Source name: {}
								Source display name: {}
								Source entity UUID: {}
								Source position: {}
								""",
							((ServerCommandSourceAccessor) source).getOutput().getClass(),
							source.getName(),
							source.getDisplayName(),
							source.getEntity() instanceof Entity entity ? entity.getUuid() : "(no entity)",
							source.getPosition()
						);
						yield "???(Check server log)";
					}
				},
				gameProfile.getName(),
				gameProfile.getId()
			);
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create("https://discord.com/api/webhooks/%s/%s".formatted(config.webhookId, config.webhookToken)))
			.header("Content-Type", "application/json")
			.POST(BodyPublishers.ofString("""
				{ "content": "%s" }
				""".formatted(formattedMsg)))
			.timeout(Duration.ofMinutes(1))
			.build();
		httpClient.sendAsync(request, BodyHandlers.ofString())
			.handleAsync((response, throwable) -> {
				if (throwable != null) {
					LOGGER.warn("Exception while sending a webhook request", throwable);
					LOGGER.info("Request content: {}", formattedMsg);
				}
				return response;
			}, source.getServer())
			/*.thenApply(HttpResponse::body)
			.thenAccept(LOGGER::info)*/;
	}

	@Inject(method = "executeAdd", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/ServerCommandSource;sendFeedback(Ljava/util/function/Supplier;Z)V"))
	private static void sendAddMessage(CallbackInfoReturnable<Integer> cir, @Local(argsOnly = true) ServerCommandSource source, @Local GameProfile gameProfile) {
		sendMessage("[%s] %s added [%s](<https://namemc.com/profile/%s>) to the whitelist.", source, gameProfile);
	}

	@Inject(method = "executeRemove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/ServerCommandSource;sendFeedback(Ljava/util/function/Supplier;Z)V"))
	private static void sendRemoveMessage(CallbackInfoReturnable<Integer> cir, @Local(argsOnly = true) ServerCommandSource source, @Local GameProfile gameProfile) {
		sendMessage("[%s] %s removed [%s](<https://namemc.com/profile/%s>) from the whitelist.", source, gameProfile);
	}
}
