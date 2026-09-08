package rem.coffenmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

public class CoffinBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<CoffinPart> PART = EnumProperty.create("part", CoffinPart.class);
    private static final VoxelShape SHAPE = Block.box(0,0,0,16,8,16);
    private final int coffinType;

    public CoffinBlock(Properties properties, int coffinType) {
        super(properties); this.coffinType = coffinType;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, CoffinPart.FOOT));
    }
    public int getCoffinType(){ return coffinType; }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b){ b.add(FACING,PART); }
    @Override public RenderShape getRenderShape(BlockState state){ return RenderShape.INVISIBLE; }
    @Override public VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c){ return SHAPE; }

    @Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext ctx){
        Direction f=ctx.getHorizontalDirection(); BlockPos head=ctx.getClickedPos().relative(f);
        if(!ctx.getLevel().getBlockState(head).canBeReplaced(ctx)) return null;
        return defaultBlockState().setValue(FACING,f).setValue(PART,CoffinPart.FOOT);
    }

    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack){
        super.setPlacedBy(level,pos,state,placer,stack);
        if(level.isClientSide) return;
        BlockPos head=pos.relative(state.getValue(FACING)); level.setBlock(head,state.setValue(PART,CoffinPart.HEAD),3);
        if(level.getBlockEntity(pos) instanceof CoffinBlockEntity be){
            be.setCoffinType(coffinType);
            if(stack.hasTag() && stack.getTag().contains("pname")) be.setOwnerName(stack.getTag().getString("pname"));
        }
    }

    private BlockPos foot(BlockPos p, BlockState s){ return s.getValue(PART)==CoffinPart.FOOT?p:p.relative(s.getValue(FACING).getOpposite()); }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        BlockPos foot = foot(pos, state);
        BlockState footState = level.getBlockState(foot);

        if (!(level.getBlockEntity(foot) instanceof CoffinBlockEntity be)) {
            return InteractionResult.PASS;
        }

        // Keep the existing shift-right-click editor.
        if (player.isShiftKeyDown()) {
            if (level.isClientSide) {
                DistExecutor.unsafeRunWhenOn(
                        Dist.CLIENT,
                        () -> () -> rem.coffenmod.client.ClientHooks.openCoffinEditor(foot, be.getOwnerName())
                );
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide) {
            // Open the coffin lid.
            be.toggleLid(findOpenDirection(level, foot, footState.getValue(FACING)));

            // Open a real 27-slot coffin inventory.
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (containerId, playerInventory, menuPlayer) ->
                                ChestMenu.threeRows(containerId, playerInventory, be),
                        Component.literal("Coffin")
                ));
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static int findOpenDirection(Level level, BlockPos foot, Direction facing){
        Direction right=facing.getClockWise(), left=facing.getCounterClockWise(); BlockPos head=foot.relative(facing);
        if(level.getBlockState(foot.relative(right)).isAir() && level.getBlockState(head.relative(right)).isAir()) return 0;
        if(level.getBlockState(foot.relative(left)).isAir() && level.getBlockState(head.relative(left)).isAir()) return 2;
        if(level.getBlockState(foot.relative(left).above()).isAir() && level.getBlockState(head.relative(left).above()).isAir()) return 3;
        return 1;
    }

    @Override public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player){
        BlockPos foot=foot(pos,state);
        BlockPos other = state.getValue(PART)==CoffinPart.FOOT ? pos.relative(state.getValue(FACING)) : pos.relative(state.getValue(FACING).getOpposite());
        if(level.getBlockState(other).getBlock()==this) level.destroyBlock(other,false);
        super.playerWillDestroy(level,pos,state,player);
    }

    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state){ return state.getValue(PART)==CoffinPart.FOOT?new CoffinBlockEntity(pos,state):null; }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type){
        return state.getValue(PART)==CoffinPart.FOOT && type==Coffenmod.COFFIN_BLOCK_ENTITY.get() ? (l,p,s,b)->CoffinBlockEntity.tick(l,p,s,(CoffinBlockEntity)b) : null;
    }
}
