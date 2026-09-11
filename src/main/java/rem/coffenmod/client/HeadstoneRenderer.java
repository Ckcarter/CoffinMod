package rem.coffenmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import rem.coffenmod.HeadstoneBlock;
import rem.coffenmod.HeadstoneBlockEntity;

import java.util.ArrayList;
import java.util.List;

public class HeadstoneRenderer implements BlockEntityRenderer<HeadstoneBlockEntity> {
    private final Font font;

    public HeadstoneRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(HeadstoneBlockEntity be,
                       float partialTick,
                       PoseStack ps,
                       MultiBufferSource buffers,
                       int packedLight,
                       int packedOverlay) {

        if (!be.getBlockState().hasProperty(HeadstoneBlock.FACING)) {
            return;
        }

        Direction facing = be.getBlockState().getValue(HeadstoneBlock.FACING);

        List<String> lines = new ArrayList<>();
        lines.add("R.I.P.");
        lines.addAll(wrapToPixelWidth(
                be.getDeathReason(),
                82,
                5
        ));

        ps.pushPose();

        switch (facing) {
            case NORTH -> {
                ps.translate(0.5D, 1.005D, 0.3115D);
                ps.mulPose(Axis.YP.rotationDegrees(0.0F));
            }
            case SOUTH -> {
                ps.translate(0.5D, 1.005D, 0.6885D);
                ps.mulPose(Axis.YP.rotationDegrees(180.0F));
            }
            case WEST -> {
                ps.translate(0.3115D, 1.005D, 0.5D);
                ps.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case EAST -> {
                ps.translate(0.6885D, 1.005D, 0.5D);
                ps.mulPose(Axis.YP.rotationDegrees(-90.0F));
            }
            default -> {
                ps.translate(0.5D, 1.005D, 0.3115D);
            }
        }

        float textScale = 0.0090F;
        ps.scale(-textScale, -textScale, textScale);

        float lineHeight = 10.0F;
        float startY = -((lines.size() - 1) * lineHeight) / 2.0F;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            font.drawInBatch(
                    line,
                    -font.width(line) / 2.0F,
                    startY + (i * lineHeight),
                    0xFFFFFFFF,
                    false,
                    ps.last().pose(),
                    buffers,
                    Font.DisplayMode.POLYGON_OFFSET,
                    0,
                    LightTexture.FULL_BRIGHT
            );
        }

        ps.popPose();

        renderFlower(be, facing, buffers, packedLight);
    }

    private void renderFlower(HeadstoneBlockEntity be,
                              Direction facing,
                              MultiBufferSource buffers,
                              int packedLight) {

        if (be.getFlower().isEmpty()) {
            return;
        }

        if (!(be.getFlower().getItem() instanceof BlockItem blockItem)) {
            return;
        }

        PoseStack ps = new PoseStack();

        /*
         * FLOWER POSITION
         *
         * Put the flower clearly OUTSIDE the cobblestone face.
         * The headstone is about 6px thick, so these coordinates are
         * deliberately farther out than the lettering plane.
         */
        switch (facing) {
            case NORTH -> ps.translate(0.5D, 0.12D, 0.245D);
            case SOUTH -> ps.translate(0.5D, 0.12D, 0.755D);
            case WEST  -> ps.translate(0.245D, 0.12D, 0.5D);
            case EAST  -> ps.translate(0.755D, 0.12D, 0.5D);
            default    -> ps.translate(0.5D, 0.12D, 0.245D);
        }

        /*
         * renderSingleBlock renders block geometry from 0..1 coordinates.
         * Center that geometry on our attachment point before scaling.
         */
        final float flowerScale = 0.60F;
        ps.scale(flowerScale, flowerScale, flowerScale);
        ps.translate(-0.5D, 0.0D, -0.5D);

        Minecraft.getInstance()
                .getBlockRenderer()
                .renderSingleBlock(
                        blockItem.getBlock().defaultBlockState(),
                        ps,
                        buffers,
                        LightTexture.FULL_BRIGHT,
                        OverlayTexture.NO_OVERLAY
                );
    }

    private List<String> wrapToPixelWidth(String text, int maxPixelWidth, int maxLines) {
        List<String> result = new ArrayList<>();

        String remaining = (text == null || text.isBlank())
                ? "Unknown cause"
                : text.trim().replaceAll("\\s+", " ");

        while (!remaining.isEmpty() && result.size() < maxLines) {
            if (font.width(remaining) <= maxPixelWidth) {
                result.add(remaining);
                remaining = "";
                break;
            }

            int bestBreak = -1;
            int lastSpace = -1;

            for (int i = 1; i <= remaining.length(); i++) {
                String candidate = remaining.substring(0, i);

                if (font.width(candidate) > maxPixelWidth) {
                    break;
                }

                bestBreak = i;

                if (Character.isWhitespace(remaining.charAt(i - 1))) {
                    lastSpace = i - 1;
                }
            }

            int breakAt = lastSpace > 0 ? lastSpace : bestBreak;
            if (breakAt <= 0) {
                breakAt = 1;
            }

            String line = remaining.substring(0, breakAt).trim();
            if (!line.isEmpty()) {
                result.add(line);
            }

            remaining = remaining.substring(breakAt).trim();
        }

        if (!remaining.isEmpty() && !result.isEmpty()) {
            int lastIndex = result.size() - 1;
            String last = result.get(lastIndex);

            while (!last.isEmpty() && font.width(last + "...") > maxPixelWidth) {
                last = last.substring(0, last.length() - 1);
            }

            result.set(lastIndex, last + "...");
        }

        return result;
    }
}
