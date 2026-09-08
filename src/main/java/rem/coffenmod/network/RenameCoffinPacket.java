package rem.coffenmod.network;
import net.minecraft.core.BlockPos; import net.minecraft.network.FriendlyByteBuf; import net.minecraftforge.network.NetworkEvent; import rem.coffenmod.CoffinBlockEntity; import java.util.function.Supplier;
public record RenameCoffinPacket(BlockPos pos,String name){
 public static void encode(RenameCoffinPacket p,FriendlyByteBuf b){b.writeBlockPos(p.pos);b.writeUtf(p.name,24);} public static RenameCoffinPacket decode(FriendlyByteBuf b){return new RenameCoffinPacket(b.readBlockPos(),b.readUtf(24));}
 public static void handle(RenameCoffinPacket p,Supplier<NetworkEvent.Context> s){var c=s.get(); c.enqueueWork(()->{var sp=c.getSender(); if(sp!=null && sp.distanceToSqr(p.pos.getX()+.5,p.pos.getY()+.5,p.pos.getZ()+.5)<64 && sp.level().getBlockEntity(p.pos) instanceof CoffinBlockEntity be) be.setOwnerName(p.name);}); c.setPacketHandled(true);}
}
