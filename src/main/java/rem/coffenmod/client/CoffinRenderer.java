package rem.coffenmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import rem.coffenmod.Coffenmod;
import rem.coffenmod.CoffinBlock;
import rem.coffenmod.CoffinBlockEntity;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Renderer rebuilt from the original 1.7.10 RenderCoffin transform sequence.
 * The direction-specific translations/scales are deliberate: the old renderer
 * used four different transforms rather than rotating one centered model.
 */
public class CoffinRenderer implements BlockEntityRenderer<CoffinBlockEntity> {
    private static final ResourceLocation STONE_TEXTURE =
            new ResourceLocation(Coffenmod.MODID, "textures/entity/coffin.png");
    private static final ResourceLocation WOOD_TEXTURE =
            new ResourceLocation(Coffenmod.MODID, "textures/entity/coffin_wood.png");

    private final CoffinModel model;
    private final PlayerModel<net.minecraft.world.entity.LivingEntity> playerModel;

    public CoffinRenderer(BlockEntityRendererProvider.Context ctx) {
        this.model = new CoffinModel(ctx.bakeLayer(ClientModEvents.COFFIN_LAYER));
        this.playerModel = new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER), false);
    }

    @Override
    public void render(CoffinBlockEntity be, float partialTick, PoseStack ps,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        BlockState state = be.getBlockState();
        if (!state.hasProperty(CoffinBlock.FACING)) return;

        Direction facing = state.getValue(CoffinBlock.FACING);

        // Original RenderCoffin rendered the corpse first and the coffin second.
        if (!be.getOwnerName().isBlank()) {
            renderBody(be, facing, ps, buffers, packedLight);
        }

        renderCoffin(be, facing, ps, buffers, packedLight);
    }

    private void renderCoffin(CoffinBlockEntity be, Direction facing, PoseStack ps,
                              MultiBufferSource buffers, int packedLight) {
        ps.pushPose();

        // Exact world-space origin from RenderCoffin.java:
        // glTranslatef(x - .765, y + .4, z + .075)
        ps.translate(-0.765F, 0.4F, 0.075F);

        // Metadata 0/1/2/3 in 1.7.10 corresponded to SOUTH/WEST/NORTH/EAST.
        switch (facing) {
            case SOUTH -> {
                ps.scale(1.5F, 1.5F, 1.36F);
                ps.translate(1.0F, 0.0F, 0.0F);
            }
            case WEST -> {
                ps.scale(1.36F, 1.5F, 1.5F);
                ps.mulPose(Axis.YP.rotationDegrees(-90.0F));
                ps.translate(0.44F, 0.0F, -1.24F);
            }
            case NORTH -> {
                ps.scale(1.5F, 1.5F, 1.36F);
                ps.mulPose(Axis.YP.rotationDegrees(-180.0F));
                ps.translate(-0.686F, 0.0F, -0.62F);
            }
            case EAST -> {
                ps.scale(1.36F, 1.5F, 1.5F);
                ps.mulPose(Axis.YP.rotationDegrees(90.0F));
                ps.translate(-0.127F, 0.0F, 0.62F);
            }
            default -> {
            }
        }

        // Shape15 was the moving lid parent in the original ModelCoffin.
        model.lid.x = be.pos;
        model.lid.y = be.posY;
        model.lid.z = 11.0F;
        model.lid.xRot = 0.0F;
        model.lid.yRot = 0.0F;
        model.lid.zRot = be.rot;

        // Original final transform before ModelCoffin.render().  This is what
        // makes the old ModelRenderer +Y-down geometry sit correctly in-world.
        ps.mulPose(Axis.ZP.rotationDegrees(180.0F));

        ResourceLocation texture = be.getCoffinType() == 1 ? WOOD_TEXTURE : STONE_TEXTURE;
        model.root.render(ps,
                buffers.getBuffer(RenderType.entityCutoutNoCull(texture)),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);

        ps.popPose();
    }

    private void renderBody(CoffinBlockEntity be, Direction facing, PoseStack ps,
                            MultiBufferSource buffers, int packedLight) {
        ps.pushPose();

        /*
         * LOCK the corpse to the visual center of the rendered coffin cavity.
         *
         * FOOT block center = (0.5, 0.5)
         * HEAD block center = one block in FACING
         * The old coffin model's visual cavity is about 0.90 block toward FACING from the FOOT center.
         *
         * Do not use the old 1.7.10 metadata translations here; those offsets
         * are what caused the modern PlayerModel to slide outside the coffin.
         */
        // Move corpse 4 pixels toward its feet
        double corpseOffset = 0.078D - 0.25D;

        double centerX = 0.5D + facing.getStepX() * corpseOffset;
        double centerZ = 0.5D + facing.getStepZ() * corpseOffset;

        // Keep the body down inside the coffin cavity.
        ps.translate(centerX, 0.145D, centerZ);

        /*
         * Rotate only around the coffin's center. No directional translations
         * occur after this point, so the corpse cannot move away from the box.
         */
        float yaw = switch (facing) {
            case SOUTH -> 0.0F;
            case WEST  -> -90.0F;
            case NORTH -> 180.0F;
            case EAST  -> 90.0F;
            default -> 0.0F;
        };
        ps.mulPose(Axis.YP.rotationDegrees(yaw));

        // Lay the player flat, face-up.
        ps.mulPose(Axis.XP.rotationDegrees(90.0F));

        /*
         * Slightly smaller than vanilla so arms, head and feet stay inside
         * the narrow coffin walls.
         */
        // Corpse size controls
        float bodyWidth = 1.11F;
        float bodyLength = 1.22F;
        float bodyThickness = 1.11F;

// X = width
// Y = length after the body has been laid flat
// Z = thickness
        ps.scale(
                -bodyWidth,
                -bodyLength,
                bodyThickness
        );
        // Standard PlayerModel origin correction.
        ps.translate(0.0D, -1.501D, 0.0D);

        // Motionless corpse pose.
        playerModel.setAllVisible(true);

        // Keep the corpse head attached to the body at the normal vanilla pivot.
        playerModel.head.x = 0.0F;
        playerModel.head.y = 0.0F;
        playerModel.head.z = 0.0F;
        playerModel.head.xRot = 0.0F;
        playerModel.head.yRot = 0.0F;
        playerModel.head.zRot = 0.0F;

        // The outer head/hat layer must use the exact same pivot and rotation.
        playerModel.hat.copyFrom(playerModel.head);
        playerModel.hat.x = playerModel.head.x;
        playerModel.hat.y = playerModel.head.y;
        playerModel.hat.z = playerModel.head.z;
        playerModel.hat.visible = true;

        playerModel.body.xRot = 0.0F;
        playerModel.body.yRot = 0.0F;
        playerModel.body.zRot = 0.0F;

        playerModel.rightArm.xRot = 0.0F;
        playerModel.rightArm.yRot = 0.0F;
        playerModel.rightArm.zRot = 0.0F;
        playerModel.leftArm.xRot = 0.0F;
        playerModel.leftArm.yRot = 0.0F;
        playerModel.leftArm.zRot = 0.0F;

        playerModel.rightLeg.xRot = 0.0F;
        playerModel.rightLeg.yRot = 0.0F;
        playerModel.rightLeg.zRot = 0.0F;
        playerModel.leftLeg.xRot = 0.0F;
        playerModel.leftLeg.yRot = 0.0F;
        playerModel.leftLeg.zRot = 0.0F;

        ResourceLocation skin = skin(be.getOwnerName());

        var skinBuffer = buffers.getBuffer(RenderType.entityCutoutNoCull(skin));

        // Render the normal corpse model first.
        playerModel.renderToBuffer(
                ps,
                skinBuffer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F
        );

        /*
         * Align ALL outer player skin layers to the exact corpse pose.
         * Each outer layer copies the transform of its matching base part.
         */
        // Re-sync hat to the final head transform immediately before overlay rendering.
        playerModel.hat.copyFrom(playerModel.head);
        playerModel.jacket.copyFrom(playerModel.body);
        playerModel.leftSleeve.copyFrom(playerModel.leftArm);
        playerModel.rightSleeve.copyFrom(playerModel.rightArm);
        playerModel.leftPants.copyFrom(playerModel.leftLeg);
        playerModel.rightPants.copyFrom(playerModel.rightLeg);

        playerModel.hat.visible = true;
        playerModel.jacket.visible = true;
        playerModel.leftSleeve.visible = true;
        playerModel.rightSleeve.visible = true;
        playerModel.leftPants.visible = true;
        playerModel.rightPants.visible = true;

        /*
         * Render the outer layers explicitly so every overlay stays lined up
         * with the corpse body, even when the normal PlayerModel pass skips one.
         */
        playerModel.hat.render(
                ps, skinBuffer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F
        );

        playerModel.jacket.render(
                ps, skinBuffer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F
        );

        playerModel.leftSleeve.render(
                ps, skinBuffer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F
        );

        playerModel.rightSleeve.render(
                ps, skinBuffer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F
        );

        playerModel.leftPants.render(
                ps, skinBuffer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F
        );

        playerModel.rightPants.render(
                ps, skinBuffer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F
        );

        ps.popPose();
    }

    private ResourceLocation skin(String name) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            var info = connection.getPlayerInfo(name);
            if (info != null) return info.getSkinLocation();
        }

        UUID id = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        return DefaultPlayerSkin.getDefaultSkin(id);
    }
}
