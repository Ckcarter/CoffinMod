package rem.coffenmod;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class HeadstoneBlockEntity extends BlockEntity {
    private String playerName = "";
    private String deathReason = "";
    private ItemStack flower = ItemStack.EMPTY;

    public HeadstoneBlockEntity(BlockPos pos, BlockState state) {
        super(Coffenmod.HEADSTONE_BLOCK_ENTITY.get(), pos, state);
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getDeathReason() {
        return deathReason;
    }

    public ItemStack getFlower() {
        return flower;
    }

    public void setFlower(ItemStack stack) {
        flower = stack == null || stack.isEmpty()
                ? ItemStack.EMPTY
                : stack.copyWithCount(1);
        sync();
    }

    public void setMemorialText(String playerName, String deathReason) {
        this.playerName = playerName == null ? "" : playerName;
        this.deathReason = (deathReason == null || deathReason.isBlank())
                ? "Unknown cause"
                : deathReason;
        sync();
    }

    private void sync() {
        setChanged();

        if (level != null) {
            level.sendBlockUpdated(
                    worldPosition,
                    getBlockState(),
                    getBlockState(),
                    3
            );
        }
    }

    public CompoundTag saveMemorialData() {
        CompoundTag tag = new CompoundTag();

        tag.putString("PlayerName", playerName);
        tag.putString("DeathReason", deathReason);

        if (!flower.isEmpty()) {
            tag.put("Flower", flower.save(new CompoundTag()));
        }

        return tag;
    }

    public void loadMemorialData(CompoundTag tag) {
        playerName = tag.getString("PlayerName");
        deathReason = tag.getString("DeathReason");

        flower = tag.contains("Flower")
                ? ItemStack.of(tag.getCompound("Flower"))
                : ItemStack.EMPTY;

        sync();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putString("PlayerName", playerName);
        tag.putString("DeathReason", deathReason);

        if (!flower.isEmpty()) {
            tag.put("Flower", flower.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        playerName = tag.getString("PlayerName");
        deathReason = tag.getString("DeathReason");

        flower = tag.contains("Flower")
                ? ItemStack.of(tag.getCompound("Flower"))
                : ItemStack.EMPTY;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            load(tag);
        }
    }
}
