package rem.coffenmod;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SpecterArrowEntity extends AbstractArrow {
    public SpecterArrowEntity(EntityType<? extends SpecterArrowEntity> type, Level level) {
        super(type, level);
    }

    public SpecterArrowEntity(Level level, LivingEntity owner) {
        super(Coffenmod.SPECTER_ARROW.get(), owner, level);
        setBaseDamage(2.0D);
    }

    @Override protected ItemStack getPickupItem() { return ItemStack.EMPTY; }

    @Override public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount >= 200) discard();
    }
}
