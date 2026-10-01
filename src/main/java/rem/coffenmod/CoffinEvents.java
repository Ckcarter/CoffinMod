package rem.coffenmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.ModList;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;

/** Ports the original 1.7.10 grave-on-death behavior. */
public class CoffinEvents {
    private static final String GRAVESTONE_MOD_ID = "gravestone";
    private static final String GRAVESTONE_COFFIN_HANDLED = "coffenmod_gravestone_coffin_handled";

    /**
     * Gravestone integration path.  Run before Gravestone's normal death handler:
     * the coffin takes ownership of the player's inventory, then Gravestone is
     * free to create its grave marker with an empty inventory.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDeathWithGravestone(LivingDeathEvent event) {
        if (!ModList.get().isLoaded(GRAVESTONE_MOD_ID)) return;
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide || event.isCanceled()) return;

        Level level = player.level();
        Direction facing = player.getDirection();
        if (!facing.getAxis().isHorizontal()) facing = Direction.NORTH;

        BlockPos surface = findSurface(level, player.blockPosition());
        if (surface == null) return;

        Direction coffinFacing = facing.getOpposite();
        // Bury the coffin one block deeper. Keep the surface/death position just
        // beyond the HEAD end so Gravestone's marker appears at the front of the coffin.
        BlockPos coffinHead = surface.relative(coffinFacing.getOpposite()).below(4);
        BlockPos coffinFoot = surface.relative(coffinFacing.getOpposite(), 2).below(4);
        if (coffinFoot.getY() <= level.getMinBuildHeight() || coffinHead.getY() <= level.getMinBuildHeight()) return;

        int type = player.getRandom().nextBoolean() ? 0 : 1;
        CoffinBlock block = (CoffinBlock) (type == 0 ? Coffenmod.STONE_COFFIN.get() : Coffenmod.WOOD_COFFIN.get());
        BlockState footState = block.defaultBlockState()
                .setValue(CoffinBlock.FACING, coffinFacing)
                .setValue(CoffinBlock.PART, CoffinPart.FOOT);

        level.setBlock(coffinFoot, footState, 3);
        level.setBlock(coffinHead, footState.setValue(CoffinBlock.PART, CoffinPart.HEAD), 3);

        List<ItemStack> stored = new ArrayList<>();
        for (ItemStack stack : player.getInventory().items) if (!stack.isEmpty()) stored.add(stack.copy());
        for (ItemStack stack : player.getInventory().armor) if (!stack.isEmpty()) stored.add(stack.copy());
        for (ItemStack stack : player.getInventory().offhand) if (!stack.isEmpty()) stored.add(stack.copy());

        if (level.getBlockEntity(coffinFoot) instanceof CoffinBlockEntity coffin) {
            coffin.setOwnerProfile(player.getGameProfile());
            coffin.setCoffinType(type);
            coffin.setSpawnGhost(false);
            coffin.setStoredStacks(stored);
            coffin.addPlayerHead(player.getGameProfile().getName());
            coffin.setChanged();
        }

        // Remove the inventory from the player so the installed Gravestone version creates only
        // the memorial marker and does not duplicate/store the same items.
        player.getInventory().clearContent();
        player.getPersistentData().putBoolean(GRAVESTONE_COFFIN_HANDLED, true);

        // Keep the Coffin Mod's buried grave mound, but do not create its own
        // headstone; Gravestone owns the visible death marker in this mode.
        BlockPos moundFoot = coffinFoot.above();
        BlockPos moundHead = coffinHead.above();
        level.setBlock(moundFoot, Blocks.DIRT.defaultBlockState(), 3);
        level.setBlock(moundHead, Blocks.DIRT.defaultBlockState(), 3);
        level.setBlock(moundFoot.above(), Blocks.MYCELIUM.defaultBlockState(), 3);
        level.setBlock(moundHead.above(), Blocks.MYCELIUM.defaultBlockState(), 3);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide || event.isCanceled()) return;

        if (ModList.get().isLoaded(GRAVESTONE_MOD_ID)
                && player.getPersistentData().getBoolean(GRAVESTONE_COFFIN_HANDLED)) {
            player.getPersistentData().remove(GRAVESTONE_COFFIN_HANDLED);
            return;
        }

        Level level = player.level();
        String deathReason = player.getCombatTracker().getDeathMessage().getString();
        if (deathReason == null || deathReason.isBlank()) {
            deathReason = "Unknown cause";
        }
        Direction facing = player.getDirection();
        if (!facing.getAxis().isHorizontal()) facing = Direction.NORTH;

        BlockPos surface = findSurface(level, player.blockPosition());
        if (surface == null) return;

        /*
         * The original mod buried the coffin by replacing underground terrain.
         * Do NOT require these blocks to be air/replaceable; that prevented
         * graves from spawning in normal dirt and stone.
         */
        Direction coffinFacing = facing.getOpposite();
        // Bury the coffin one block deeper. Keep the surface/death position just
        // beyond the HEAD end so Gravestone's marker appears at the front of the coffin.
        BlockPos coffinHead = surface.relative(coffinFacing.getOpposite()).below(4);
        BlockPos coffinFoot = surface.relative(coffinFacing.getOpposite(), 2).below(4);

        if (coffinFoot.getY() <= level.getMinBuildHeight()
                || coffinHead.getY() <= level.getMinBuildHeight()) {
            return;
        }

        int type = player.getRandom().nextBoolean() ? 0 : 1;
        CoffinBlock block = (CoffinBlock) (type == 0 ? Coffenmod.STONE_COFFIN.get() : Coffenmod.WOOD_COFFIN.get());
        BlockState footState = block.defaultBlockState()
                .setValue(CoffinBlock.FACING, coffinFacing)
                .setValue(CoffinBlock.PART, CoffinPart.FOOT);

        level.setBlock(coffinFoot, footState, 3);
        level.setBlock(coffinHead, footState.setValue(CoffinBlock.PART, CoffinPart.HEAD), 3);

        if (level.getBlockEntity(coffinFoot) instanceof CoffinBlockEntity coffin) {
            coffin.setOwnerProfile(player.getGameProfile());
            coffin.setCoffinType(type);
            coffin.setSpawnGhost(false);
            coffin.setStoredDrops(event.getDrops());
            coffin.addPlayerHead(player.getGameProfile().getName());
            coffin.setChanged();
        }

        // Recreate the original dirt/mycelium grave mound above the buried coffin.
        BlockPos moundFoot = coffinFoot.above();
        BlockPos moundHead = coffinHead.above();
        level.setBlock(moundFoot, Blocks.DIRT.defaultBlockState(), 3);
        level.setBlock(moundHead, Blocks.DIRT.defaultBlockState(), 3);
        level.setBlock(moundFoot.above(), Blocks.MYCELIUM.defaultBlockState(), 3);
        level.setBlock(moundHead.above(), Blocks.MYCELIUM.defaultBlockState(), 3);

        if (Config.HEADSTONE.get()) {
            createHeadstone(
                    level,
                    coffinHead,
                    coffinFacing,
                    player.getGameProfile().getName(),
                    deathReason
            );
        }

        // The coffin owns the drops now, just like PlayerDropsEvent cancellation in 1.7.10.
        for (ItemEntity item : event.getDrops()) item.discard();
        event.getDrops().clear();
        event.setCanceled(true);
    }


    private static BlockPos findSurface(Level level, BlockPos start) {
        BlockPos pos = start;
        int min = level.getMinBuildHeight() + 4;
        int max = level.getMaxBuildHeight() - 2;
        if (pos.getY() < min) pos = new BlockPos(pos.getX(), min, pos.getZ());
        if (pos.getY() > max) pos = new BlockPos(pos.getX(), max, pos.getZ());

        while (pos.getY() > min && level.getBlockState(pos.below()).isAir()) pos = pos.below();
        while (pos.getY() < max && !level.getBlockState(pos).canBeReplaced()) pos = pos.above();
        return pos.getY() >= min && pos.getY() <= max ? pos : null;
    }

    private static void createHeadstone(Level level,
                                        BlockPos coffinHead,
                                        Direction coffinFacing,
                                        String playerName,
                                        String deathReason) {

        BlockPos headstonePos = coffinHead.above(4).relative(coffinFacing);

        BlockState headstoneState = Coffenmod.HEADSTONE.get()
                .defaultBlockState()
                .setValue(HeadstoneBlock.FACING, coffinFacing.getOpposite());

        level.setBlock(headstonePos, headstoneState, 3);

        if (level.getBlockEntity(headstonePos) instanceof HeadstoneBlockEntity headstone) {
            headstone.setMemorialText(playerName, deathReason);
        }
    }
}
