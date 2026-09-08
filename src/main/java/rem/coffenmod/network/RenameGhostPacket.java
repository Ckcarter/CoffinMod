package rem.coffenmod.network;
import net.minecraft.network.FriendlyByteBuf; import net.minecraftforge.network.NetworkEvent; import rem.coffenmod.GhostEntity; import java.util.function.Supplier;
public record RenameGhostPacket(int id,String name){
 public static void encode(RenameGhostPacket p,FriendlyByteBuf b){b.writeVarInt(p.id);b.writeUtf(p.name,24);} public static RenameGhostPacket decode(FriendlyByteBuf b){return new RenameGhostPacket(b.readVarInt(),b.readUtf(24));}
 public static void handle(RenameGhostPacket p,Supplier<NetworkEvent.Context> s){var c=s.get(); c.enqueueWork(()->{var sp=c.getSender(); if(sp!=null && sp.level().getEntity(p.id) instanceof GhostEntity g && sp.distanceToSqr(g)<64 && sp.isCreative()) g.setOwnerName(p.name);}); c.setPacketHandled(true);}
}
