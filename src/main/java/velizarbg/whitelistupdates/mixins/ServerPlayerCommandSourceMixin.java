package velizarbg.whitelistupdates.mixins;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import velizarbg.whitelistupdates.PlayerCommandSource;

@Mixin(targets = "net.minecraft.server.level.ServerPlayer$3")
public class ServerPlayerCommandSourceMixin implements PlayerCommandSource {
	@Shadow
	@Final
	ServerPlayer this$0;

	public ServerPlayer whitelistupdates$getPlayer() {
		return this$0;
	}
}
