package rem.coffenmod;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(Coffenmod.MODID)
public class Coffenmod {
    public static final String MODID = "coffenmod";

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<Block> STONE_COFFIN = BLOCKS.register("stone_coffin", () ->
            new CoffinBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0F, 6.0F).noOcclusion().sound(SoundType.STONE), 0));
    public static final RegistryObject<Block> WOOD_COFFIN = BLOCKS.register("wood_coffin", () ->
            new CoffinBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F, 3.0F).noOcclusion().sound(SoundType.WOOD), 1));

    public static final RegistryObject<Block> HEADSTONE = BLOCKS.register("headstone", () ->
            new HeadstoneBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(2.0F, 6.0F)
                            .sound(SoundType.STONE)
                            .noOcclusion()
            ));

    public static final RegistryObject<Item> STONE_COFFIN_ITEM = ITEMS.register("stone_coffin", () -> new CoffinItem(STONE_COFFIN.get(), new Item.Properties()));
    public static final RegistryObject<Item> WOOD_COFFIN_ITEM = ITEMS.register("wood_coffin", () -> new CoffinItem(WOOD_COFFIN.get(), new Item.Properties()));

    public static final RegistryObject<Item> HEADSTONE_ITEM = ITEMS.register("headstone", () ->
            new BlockItem(HEADSTONE.get(), new Item.Properties()));

    public static final RegistryObject<BlockEntityType<CoffinBlockEntity>> COFFIN_BLOCK_ENTITY = BLOCK_ENTITIES.register("coffin", () ->
            BlockEntityType.Builder.of(CoffinBlockEntity::new, STONE_COFFIN.get(), WOOD_COFFIN.get()).build(null));

    public static final RegistryObject<BlockEntityType<HeadstoneBlockEntity>> HEADSTONE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("headstone", () ->
                    BlockEntityType.Builder.of(
                            HeadstoneBlockEntity::new,
                            HEADSTONE.get()
                    ).build(null));

    public static final RegistryObject<EntityType<GhostEntity>> GHOST = ENTITIES.register("ghost", () ->
            EntityType.Builder.of(GhostEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(8).updateInterval(2).build(MODID + ":ghost"));

    public static final RegistryObject<EntityType<SpecterArrowEntity>> SPECTER_ARROW = ENTITIES.register("specter_arrow", () ->
            EntityType.Builder.<SpecterArrowEntity>of(SpecterArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20).build(MODID + ":specter_arrow"));

    public static final RegistryObject<CreativeModeTab> COFFIN_TAB = TABS.register("coffin", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.FUNCTIONAL_BLOCKS)
            .icon(() -> STONE_COFFIN_ITEM.get().getDefaultInstance())
            .title(net.minecraft.network.chat.Component.translatable("itemGroup.coffenmod"))
            .displayItems((p, out) -> {
                out.accept(STONE_COFFIN_ITEM.get());
                out.accept(WOOD_COFFIN_ITEM.get());
                out.accept(HEADSTONE_ITEM.get());
            }).build());

    public Coffenmod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        ENTITIES.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        TABS.register(modBus);
        modBus.addListener(this::attributes);
        modBus.addListener(this::creativeTabs);
        MinecraftForge.EVENT_BUS.register(new CoffinEvents());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        rem.coffenmod.network.CoffinNetwork.register();
    }

    private void attributes(EntityAttributeCreationEvent event) {
        event.put(GHOST.get(), GhostEntity.createAttributes().build());
    }

    private void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(STONE_COFFIN_ITEM.get());
            event.accept(WOOD_COFFIN_ITEM.get());
            event.accept(HEADSTONE_ITEM.get());
        }
    }
}
