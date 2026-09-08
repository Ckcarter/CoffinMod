package rem.coffenmod.client;
import net.minecraft.client.gui.GuiGraphics; import net.minecraft.client.gui.components.Button; import net.minecraft.client.gui.components.EditBox; import net.minecraft.client.gui.screens.Screen; import net.minecraft.network.chat.Component; import rem.coffenmod.network.CoffinNetwork; import rem.coffenmod.network.RenameGhostPacket;
public class GhostEditorScreen extends Screen { private final int id; private final String initial; private EditBox name;
 public GhostEditorScreen(int id,String n){super(Component.literal("Ghost"));this.id=id;initial=n==null?"":n;}
 @Override protected void init(){int x=width/2-88,y=height/2-73;name=new EditBox(font,x+10,y+10,156,20,Component.literal("Name"));name.setMaxLength(24);name.setValue(initial);addRenderableWidget(name);addRenderableWidget(Button.builder(Component.literal("Done"),b->{save();onClose();}).bounds(x+53,y+76,70,20).build());setInitialFocus(name);}
 private void save(){CoffinNetwork.CHANNEL.sendToServer(new RenameGhostPacket(id,name.getValue()));} @Override public void onClose(){save();super.onClose();} @Override public boolean isPauseScreen(){return true;} @Override public void render(GuiGraphics g,int mx,int my,float pt){renderBackground(g);super.render(g,mx,my,pt);}
}
