package rem.coffenmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.Nullable;

public class CoffinBlockEntity extends BlockEntity implements Container {
    private String ownerName = "";
    private boolean spawnGhost = false;
    private int coffinType;
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(27, ItemStack.EMPTY);

    // Original 1.7.10 TESR animation state.
    public boolean closed = false; // false = lid shut; true = opening/open, matching old field behavior
    public float rot = 0.0F;
    public float pos = -3.0F;
    public float posY = -1.0F;
    public int openDir = 0;
    private static final float[] ROT_DIR = {0.3F, -1.66F, -0.3F, -1.76F};
    private static final float[] POS_DIR = {5.0F, -2.0F, -10.0F, -2.0F};

    public CoffinBlockEntity(BlockPos pos, BlockState state) { super(Coffenmod.COFFIN_BLOCK_ENTITY.get(), pos, state); }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName == null ? "" : ownerName; sync(); }
    public boolean shouldSpawnGhost() { return false; }
    public void setSpawnGhost(boolean spawnGhost) { this.spawnGhost = false; sync(); }
    public int getCoffinType() { return coffinType; }
    public void setCoffinType(int coffinType) { this.coffinType = coffinType; sync(); }
    public NonNullList<ItemStack> getInventory() { return inventory; }

    public void setStoredDrops(java.util.Collection<net.minecraft.world.entity.item.ItemEntity> drops) {
        clearContent();
        int slot = 0;
        for (var entity : drops) {
            if (slot >= inventory.size()) break;
            if (!entity.getItem().isEmpty()) {
                inventory.set(slot++, entity.getItem().copy());
            }
        }
        sync();
    }

    public void toggleLid(int direction) {
        this.openDir = direction;
        this.closed = !this.closed;
        sync();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CoffinBlockEntity be) {
        be.animate();
        be.spawnGhost = false;
    }

    private boolean atOpenTarget() {
        return Math.abs(pos - POS_DIR[openDir]) < 0.001F && Math.abs(rot - ROT_DIR[openDir]) < 0.001F;
    }

    /**
     * Exact 1.7.10 TileEntityCoffin animation port.
     *
     * Do not replace these with a generic interpolation/approach animation:
     * the original coffin deliberately moved the lid in separate translation,
     * lift and rotation phases depending on which side had room to open.
     */
    private void animate() {
        // Original updateEntity(): direction 0, and every closing direction,
        // settles Shape15.rotationPointY back to -1 at 0.1 per tick.
        if (openDir == 0 || !closed) {
            if (posY > -1.0F) posY -= 0.1F;
            if (posY < -1.0F) posY = -1.0F;
        }

        if (openDir == 0) {
            updateRight();
        } else if (openDir == 2) {
            updateLeft();
        } else if (openDir == 3) {
            updateUp(0);
        } else {
            updateUp(1);
        }
    }

    /** Exact port of TileEntityCoffin.updateRight(). */
    private void updateRight() {
        if (closed) {
            if (pos < POS_DIR[openDir]) {
                pos += 0.2F;
            }

            if (pos == POS_DIR[openDir] && rot < ROT_DIR[openDir]) {
                rot += 0.02F;
            }

            if (rot > ROT_DIR[openDir] - 0.02F && rot < ROT_DIR[openDir] + 0.02F && rot != ROT_DIR[openDir]) {
                rot = ROT_DIR[openDir];
            }

            if (pos > POS_DIR[openDir] - 0.02F && pos < POS_DIR[openDir] + 0.02F && pos != POS_DIR[openDir]) {
                pos = POS_DIR[openDir];
            }
        } else {
            if (rot > -0.02F && rot < 0.02F && rot != 0.0F) {
                rot = 0.0F;
            }

            if (pos > -3.02F && pos < -2.98F && pos != -3.0F) {
                pos = -3.0F;
            }

            if (rot > 0.0F) {
                rot -= 0.02F;
            }

            if (rot < 0.0F) {
                rot += 0.02F;
            }

            if (rot == 0.0F) {
                if (pos > -3.0F) {
                    pos -= 0.2F;
                }

                if (pos < -3.0F) {
                    pos += 0.2F;
                }
            }
        }
    }

    /** Exact port of TileEntityCoffin.updateLeft(). */
    private void updateLeft() {
        if (closed) {
            if (pos > POS_DIR[openDir]) {
                pos -= 0.2F;
            }

            if (pos == POS_DIR[openDir]) {
                if (posY < 1.4F) {
                    posY += 0.1F;
                }

                if (posY > 1.4F) {
                    posY = 1.4F;
                }

                if (rot > ROT_DIR[openDir]) {
                    rot -= 0.02F;
                }
            }

            if (rot > ROT_DIR[openDir] - 0.02F && rot < ROT_DIR[openDir] + 0.02F && rot != ROT_DIR[openDir]) {
                rot = ROT_DIR[openDir];
            }

            if (pos > POS_DIR[openDir] - 0.02F && pos < POS_DIR[openDir] + 0.02F && pos != POS_DIR[openDir]) {
                pos = POS_DIR[openDir];
            }
        } else {
            if (rot < 0.0F) {
                rot += 0.02F;
            }

            if (rot > 0.0F) {
                rot -= 0.02F;
            }

            if (rot == 0.0F) {
                if (pos < -3.0F) {
                    pos += 0.2F;
                }

                if (pos > -3.0F) {
                    pos -= 0.2F;
                }
            }

            if (rot > -0.02F && rot < 0.02F && rot != 0.0F) {
                rot = 0.0F;
            }

            if (pos > -3.02F && pos < -2.98F && pos != -3.0F) {
                pos = -3.0F;
            }
        }
    }

    /** Exact port of TileEntityCoffin.updateUp(int). */
    private void updateUp(int type) {
        if (closed) {
            if (type == 0) {
                // Direction 3 does not lift until the lid has rotated past -1 rad.
                if (rot <= -1.0F) {
                    if (posY < 1.0F) {
                        posY += 0.1F;
                    }

                    if (posY > 1.0F) {
                        posY = 1.0F;
                    }
                }
            } else {
                // Direction 1 raises from -1 to 0 immediately while opening.
                if (posY < 0.0F) {
                    posY += 0.1F;
                }

                if (posY > 0.0F) {
                    posY = 0.0F;
                }
            }

            if (rot < ROT_DIR[openDir]) {
                rot += 0.02F;
            } else if (rot > ROT_DIR[openDir]) {
                rot -= 0.02F;
            }

            // The original up-opening directions translate at 0.1, not 0.2.
            if (pos < POS_DIR[openDir]) {
                pos += 0.1F;
            } else if (pos > POS_DIR[openDir]) {
                pos -= 0.1F;
            }

            if (rot > ROT_DIR[openDir] - 0.02F && rot < ROT_DIR[openDir] + 0.02F && rot != ROT_DIR[openDir]) {
                rot = ROT_DIR[openDir];
            }

            if (pos > POS_DIR[openDir] - 0.02F && pos < POS_DIR[openDir] + 0.02F && pos != POS_DIR[openDir]) {
                pos = POS_DIR[openDir];
            }
        } else {
            if (pos > -3.0F && rot == 0.0F) {
                pos -= 0.2F;
            } else if (pos < -3.0F && rot == 0.0F) {
                pos += 0.2F;
            }

            if (rot > 0.0F) {
                rot -= 0.02F;
            } else if (rot < 0.0F) {
                rot += 0.02F;
            }

            if (rot > -0.02F && rot < 0.02F && rot != 0.0F) {
                rot = 0.0F;
            }

            if (pos > -3.02F && pos < -2.98F && pos != -3.0F) {
                pos = -3.0F;
            }
        }
    }

    public void spawnGhost() {
        spawnGhost = false;
    }

    private void equipBest(GhostEntity ghost) {
        ItemStack bow = ItemStack.EMPTY, main = ItemStack.EMPTY;
        double bestAttack = -1;
        java.util.EnumMap<EquipmentSlot, ItemStack> armor = new java.util.EnumMap<>(EquipmentSlot.class);
        java.util.EnumMap<EquipmentSlot, Integer> defense = new java.util.EnumMap<>(EquipmentSlot.class);
        for (ItemStack stack : inventory) {
            if (stack.getItem() instanceof BowItem) bow = stack.copy();
            double attack = stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).stream().mapToDouble(m -> m.getAmount()).sum();
            if (attack > bestAttack) { bestAttack = attack; main = stack.copy(); }
            if (stack.getItem() instanceof ArmorItem ai) {
                EquipmentSlot slot = ai.getEquipmentSlot();
                if (ai.getDefense() > defense.getOrDefault(slot, -1)) { defense.put(slot, ai.getDefense()); armor.put(slot, stack.copy()); }
            }
        }
        if (Config.EQUIP_BOWS.get() && !bow.isEmpty() && (main.isEmpty() || bestAttack <= 6.0D) && ghost.getRandom().nextBoolean()) main = bow;
        if (Config.EQUIP_BOWS.get() && !bow.isEmpty() && (main.isEmpty() || bestAttack < 6.0D)) main = bow;
        if (!main.isEmpty()) ghost.setItemSlot(EquipmentSlot.MAINHAND, main);
        armor.forEach(ghost::setItemSlot);
    }

    private void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Pname", ownerName); tag.putBoolean("spawnGhost", false); tag.putInt("type", coffinType);
        tag.putBoolean("closed", closed); tag.putFloat("rot", rot); tag.putFloat("pos", pos); tag.putFloat("posY", posY); tag.putInt("openDir", openDir);
        ListTag list = new ListTag();
        for (int i=0;i<inventory.size();i++) { CompoundTag item = new CompoundTag(); item.putByte("Slot", (byte)i); inventory.get(i).save(item); list.add(item); }
        tag.put("Inventory", list);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        ownerName = tag.contains("Pname") ? tag.getString("Pname") : tag.getString("Owner");
        spawnGhost = false; coffinType = tag.contains("type") ? tag.getInt("type") : tag.getInt("Type");
        closed = tag.getBoolean("closed"); rot = tag.getFloat("rot"); pos = tag.contains("pos") ? tag.getFloat("pos") : -3F; posY = tag.contains("posY") ? tag.getFloat("posY") : -1F; openDir = tag.getInt("openDir");
        clearContent();
        ListTag list = tag.getList("Inventory", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < inventory.size()) {
                inventory.set(slot, ItemStack.of(itemTag));
            }
        }
    }

    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) { if (pkt.getTag()!=null) load(pkt.getTag()); }

    @Override
    public int getContainerSize() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < inventory.size() ? inventory.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = net.minecraft.world.ContainerHelper.removeItem(inventory, slot, amount);
        if (!result.isEmpty()) sync();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = net.minecraft.world.ContainerHelper.takeItem(inventory, slot);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= inventory.size()) return;
        inventory.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        sync();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < inventory.size(); i++) {
            inventory.set(i, ItemStack.EMPTY);
        }
        setChanged();
    }

}
