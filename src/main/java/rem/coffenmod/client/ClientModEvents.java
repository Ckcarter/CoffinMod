package rem.coffenmod.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rem.coffenmod.Coffenmod;

@Mod.EventBusSubscriber(modid = Coffenmod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    public static final ModelLayerLocation COFFIN_LAYER = new ModelLayerLocation(new ResourceLocation(Coffenmod.MODID,"coffin"),"main");
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){ e.registerLayerDefinition(COFFIN_LAYER,CoffinModel::createLayer); }
    @SubscribeEvent public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Coffenmod.COFFIN_BLOCK_ENTITY.get(), CoffinRenderer::new);
        event.registerBlockEntityRenderer(Coffenmod.HEADSTONE_BLOCK_ENTITY.get(), HeadstoneRenderer::new);
        event.registerEntityRenderer(Coffenmod.GHOST.get(), GhostRenderer::new);
        event.registerEntityRenderer(Coffenmod.SPECTER_ARROW.get(), context -> new ArrowRenderer<>(context) {
            private final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/entity/projectiles/arrow.png");
            @Override public ResourceLocation getTextureLocation(rem.coffenmod.SpecterArrowEntity entity) { return TEXTURE; }
        });
    }
}
