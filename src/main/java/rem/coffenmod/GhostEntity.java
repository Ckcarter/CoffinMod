package rem.coffenmod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class GhostEntity extends Monster {
    private static final EntityDataAccessor<String> OWNER = SynchedEntityData.defineId(GhostEntity.class, EntityDataSerializers.STRING);
    private final List<ItemStack> storedItems = new ArrayList<>();
    private int attackCooldown;

    public GhostEntity(EntityType<? extends GhostEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0D).add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D).add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override protected void defineSynchedData() { super.defineSynchedData(); entityData.define(OWNER, ""); }
    public String getOwnerName() { return entityData.get(OWNER); }
    public void setOwnerName(String name) { entityData.set(OWNER, name == null ? "" : name); }
    public void addStoredItem(ItemStack stack) { if (!stack.isEmpty()) storedItems.add(stack); }

    @Override public void tick() {
        super.tick();
        noPhysics = true;
        setNoGravity(true);
        if (attackCooldown > 0) attackCooldown--;
        if (level().isClientSide) return;
        Player target = level().getNearestPlayer(this, 40.0D);
        if (target == null || target.isCreative() || target.isSpectator() || (!Config.CHASE_OTHER_PLAYERS.get() && !target.getGameProfile().getName().equalsIgnoreCase(getOwnerName()))) {
            setDeltaMovement(getDeltaMovement().scale(0.85D));
            return;
        }
        Vec3 delta = target.getEyePosition().subtract(position());
        double dist = delta.length();
        if (dist > 0.001D) {
            Vec3 desired = delta.normalize().scale(0.32D);
            double accel = Config.GHOST_SPEED.get();
            setDeltaMovement(getDeltaMovement().lerp(desired, Math.min(1.0D, accel)));
            move(net.minecraft.world.entity.MoverType.SELF, getDeltaMovement());
            setYRot((float)(Math.atan2(getDeltaMovement().z, getDeltaMovement().x) * 180.0D / Math.PI) - 90.0F);
            yBodyRot = getYRot();
        }
        if (getMainHandItem().getItem() instanceof BowItem) {
            if (attackCooldown <= 0 && dist < 30.0D) { shootArrow(target); attackCooldown = 35; }
        } else if (attackCooldown <= 0 && dist < 1.6D) {
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(Config.GHOST_DAMAGE.get());
            doHurtTarget(target);
            attackCooldown = 20;
        }
    }

    private void shootArrow(LivingEntity target) {
        if (!(level() instanceof ServerLevel server)) return;
        SpecterArrowEntity arrow = new SpecterArrowEntity(server, this);
        double dx = target.getX() - getX();
        double dy = target.getY(0.33D) - arrow.getY();
        double dz = target.getZ() - getZ();
        double flat = Math.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + flat * 0.15D, dz, 1.6F, 8.0F);
        arrow.setBaseDamage(2.0D);
        server.addFreshEntity(arrow);
    }


    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!player.isCreative()) return InteractionResult.PASS;
        if (player.isShiftKeyDown()) {
            if (level().isClientSide) DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> rem.coffenmod.client.ClientHooks.openGhostEditor(getId(), getOwnerName()));
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (!level().isClientSide) {
            ItemStack held = player.getItemInHand(hand);
            if (!held.isEmpty()) {
                EquipmentSlot slot = Mob.getEquipmentSlotForItem(held);
                if (slot == EquipmentSlot.MAINHAND && held.getItem() instanceof net.minecraft.world.item.ArmorItem armor) slot = armor.getEquipmentSlot();
                ItemStack old = getItemBySlot(slot);
                if (!old.isEmpty()) spawnAtLocation(old.copy());
                setItemSlot(slot, held.copyWithCount(1));
                held.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player p && !p.isCreative() && !Config.OTHER_PLAYERS_CAN_KILL.get() && !p.getGameProfile().getName().equalsIgnoreCase(getOwnerName())) return false;
        return super.hurt(source, amount);
    }

    @Override protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        for (ItemStack stack : storedItems) spawnAtLocation(stack.copy());
    }

    @Override protected boolean shouldDespawnInPeaceful() { return false; }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(net.minecraft.world.entity.Entity entity) {}
    @Override public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }

    @Override protected SoundEvent getAmbientSound() {
        return switch (getRandom().nextInt(3)) {
            case 0 -> ModSounds.GHOST_SAY1.get();
            case 1 -> ModSounds.GHOST_SAY2.get();
            default -> ModSounds.GHOST_SAY3.get();
        };
    }

    @Override protected SoundEvent getHurtSound(DamageSource source) { return ModSounds.GHOST_HURT.get(); }
    @Override protected SoundEvent getDeathSound() { return ModSounds.GHOST_DEATH.get(); }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Owner", getOwnerName());
        ListTag list = new ListTag();
        for (ItemStack stack : storedItems) { CompoundTag item = new CompoundTag(); stack.save(item); list.add(item); }
        tag.put("StoredItems", list);
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setOwnerName(tag.getString("Owner"));
        storedItems.clear();
        ListTag list = tag.getList("StoredItems", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) storedItems.add(ItemStack.of(list.getCompound(i)));
    }
}
