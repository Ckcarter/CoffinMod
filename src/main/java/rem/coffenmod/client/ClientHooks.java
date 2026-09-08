package rem.coffenmod.client;
import net.minecraft.client.Minecraft; import net.minecraft.core.BlockPos;
public final class ClientHooks { public static void openCoffinEditor(BlockPos pos,String name){ Minecraft.getInstance().setScreen(new CoffinEditorScreen(pos,name)); } public static void openGhostEditor(int id,String name){ Minecraft.getInstance().setScreen(new GhostEditorScreen(id,name)); } }
