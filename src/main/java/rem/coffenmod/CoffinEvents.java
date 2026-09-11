package rem.coffenmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;

/** Ports the original 1.7.10 grave-on-death behavior. */
public class CoffinEvents {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide || event.isCanceled()) return;

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
        BlockPos coffinFoot = surface.relative(coffinFacing).below(3);
        BlockPos coffinHead = surface.relative(coffinFacing, 2).below(3);

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

        BlockPos headstonePos = coffinHead.above(3).relative(coffinFacing);

        BlockState headstoneState = Coffenmod.HEADSTONE.get()
                .defaultBlockState()
                .setValue(HeadstoneBlock.FACING, coffinFacing.getOpposite());

        level.setBlock(headstonePos, headstoneState, 3);

        if (level.getBlockEntity(headstonePos) instanceof HeadstoneBlockEntity headstone) {
            headstone.setMemorialText(playerName, deathReason);
        }
    }
}
