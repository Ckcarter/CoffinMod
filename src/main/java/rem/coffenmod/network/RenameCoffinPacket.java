package rem.coffenmod.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import rem.coffenmod.CoffinBlockEntity;

import java.util.Optional;
import java.util.function.Supplier;

public record RenameCoffinPacket(BlockPos pos, String name) {

    public static void encode(RenameCoffinPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeUtf(packet.name, 16);
    }

    public static RenameCoffinPacket decode(FriendlyByteBuf buffer) {
        return new RenameCoffinPacket(
                buffer.readBlockPos(),
                buffer.readUtf(16)
        );
    }

    public static void handle(RenameCoffinPacket packet,
                              Supplier<NetworkEvent.Context> supplier) {

        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            var player = context.getSender();

            if (player == null || !player.getAbilities().instabuild) {
                return;
            }

            if (player.distanceToSqr(
                    packet.pos.getX() + 0.5D,
                    packet.pos.getY() + 0.5D,
                    packet.pos.getZ() + 0.5D
            ) >= 64.0D) {
                return;
            }

            String requestedName = packet.name.trim();

            if (requestedName.isEmpty() || requestedName.length() > 16) {
                return;
            }

            if (!(player.level().getBlockEntity(packet.pos) instanceof CoffinBlockEntity coffin)) {
                return;
            }

            /*
             * IMPORTANT:
             * Looking up only by name on the CLIENT fails for players that
             * are not currently in the tab/player list. Resolve the actual
             * Mojang GameProfile on the SERVER instead.
             */
            Optional<GameProfile> found =
                    player.server.getProfileCache().get(requestedName);

            if (found.isEmpty()) {
                return;
            }

            GameProfile profile = found.get();

            /*
             * Fill the profile with the Mojang "textures" property.
             * That texture property is what carries the real skin URL.
             */
            GameProfile completeProfile =
                    player.server.getSessionService()
                            .fillProfileProperties(profile, false);

            coffin.setOwnerProfile(completeProfile);
        });

        context.setPacketHandled(true);
    }
}
