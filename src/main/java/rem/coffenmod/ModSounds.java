package rem.coffenmod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Coffenmod.MODID);

    public static final RegistryObject<SoundEvent> GHOST_SAY1 = register("ghost_say1");
    public static final RegistryObject<SoundEvent> GHOST_SAY2 = register("ghost_say2");
    public static final RegistryObject<SoundEvent> GHOST_SAY3 = register("ghost_say3");
    public static final RegistryObject<SoundEvent> GHOST_HURT = register("ghost_hurt");
    public static final RegistryObject<SoundEvent> GHOST_DEATH = register("ghost_death");

    private static RegistryObject<SoundEvent> register(String name) {
        ResourceLocation id = new ResourceLocation(Coffenmod.MODID, name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    private ModSounds() {}
}
