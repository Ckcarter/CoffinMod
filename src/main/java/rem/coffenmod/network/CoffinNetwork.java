package rem.coffenmod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import rem.coffenmod.Coffenmod;

public final class CoffinNetwork {
    private static final String VERSION="1";
    public static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation(Coffenmod.MODID,"main"),()->VERSION,VERSION::equals,VERSION::equals);
    private static int id;
    public static void register(){ CHANNEL.registerMessage(id++, RenameCoffinPacket.class, RenameCoffinPacket::encode, RenameCoffinPacket::decode, RenameCoffinPacket::handle); CHANNEL.registerMessage(id++, RenameGhostPacket.class, RenameGhostPacket::encode, RenameGhostPacket::decode, RenameGhostPacket::handle); }
    private CoffinNetwork(){}
}
