package rem.coffenmod;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

/** 1.20.1 counterpart to the original ItemCoffin. Owner name is carried in BlockEntityTag. */
public class CoffinItem extends BlockItem {
    public CoffinItem(Block block, Properties props) { super(block, props.stacksTo(1)); }
}
