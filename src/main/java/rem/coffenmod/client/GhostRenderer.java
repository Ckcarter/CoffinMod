package rem.coffenmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import rem.coffenmod.GhostEntity;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Player-shaped, player-skinned translucent ghost like the 1.7.10 RenderGhost. */
public class GhostRenderer extends MobRenderer<GhostEntity, HumanoidModel<GhostEntity>> {
    private final HumanoidModel<GhostEntity> inner, outer;
    public GhostRenderer(EntityRendererProvider.Context c) {
        super(c,new HumanoidModel<>(c.bakeLayer(ModelLayers.PLAYER)),0.2F);
        inner=new HumanoidModel<>(c.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)); outer=new HumanoidModel<>(c.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));
        addLayer(new HumanoidArmorLayer<>(this,inner,outer,c.getModelManager())); addLayer(new ItemInHandLayer<>(this,c.getItemInHandRenderer()));
    }
    @Override public ResourceLocation getTextureLocation(GhostEntity g){
        String name=g.getOwnerName(); var con=Minecraft.getInstance().getConnection(); if(con!=null && !name.isBlank()){var info=con.getPlayerInfo(name); if(info!=null)return info.getSkinLocation();}
        UUID id=UUID.nameUUIDFromBytes(("OfflinePlayer:"+name).getBytes(StandardCharsets.UTF_8)); return DefaultPlayerSkin.getDefaultSkin(id);
    }
    @Override protected RenderType getRenderType(GhostEntity e, boolean bodyVisible, boolean translucent, boolean glowing){ return RenderType.entityTranslucentEmissive(getTextureLocation(e)); }
    @Override protected void scale(GhostEntity entity, PoseStack poseStack, float partialTick) { poseStack.scale(0.98F,0.98F,0.98F); }
}
