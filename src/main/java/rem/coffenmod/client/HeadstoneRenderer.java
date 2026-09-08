package rem.coffenmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
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

        /*
         * Text layout:
         *
         * R.I.P.
         * Fell from a high
         * place while trying
         * to escape the
         * skeletons.
         *
         * The death reason wraps by PIXEL WIDTH, not character count,
         * so wide letters and narrow letters both fit correctly.
         */
        List<String> lines = new ArrayList<>();
        lines.add("R.I.P.");

        // Do not display the player name under R.I.P.
        // Use the freed line for one more wrapped death-reason line.
        lines.addAll(wrapToPixelWidth(
                be.getDeathReason(),
                82,
                5
        ));

        ps.pushPose();

        /*
         * Put text directly on the actual visible face of the stone.
         * These coordinates match the current 6px-thick headstone model.
         */
        switch (facing) {
            case NORTH -> {
                // Base headstone model faces NORTH, so no Y rotation is needed.
                ps.translate(0.5D, 1.13D, 0.3115D);
                ps.mulPose(Axis.YP.rotationDegrees(0.0F));
            }
            case SOUTH -> {
                // SOUTH is the NORTH-facing text plane rotated 180 degrees.
                ps.translate(0.5D, 1.13D, 0.6885D);
                ps.mulPose(Axis.YP.rotationDegrees(180.0F));
            }
            case WEST -> {
                ps.translate(0.3115D, 1.13D, 0.5D);
                ps.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case EAST -> {
                ps.translate(0.6885D, 1.13D, 0.5D);
                ps.mulPose(Axis.YP.rotationDegrees(-90.0F));
            }
            default -> {
                ps.translate(0.5D, 1.13D, 0.3115D);
                ps.mulPose(Axis.YP.rotationDegrees(0.0F));
            }
        }

        /*
         * Small enough for multiple wrapped lines, but still readable.
         * Increase this slightly later if you want larger letters.
         */
        float textScale = 0.0090F;
        ps.scale(-textScale, -textScale, textScale);

        float lineHeight = 10.0F;

        // Center the whole inscription vertically on the upper stone face.
        float startY = -((lines.size() - 1) * lineHeight) / 2.0F;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            float x = -font.width(line) / 2.0F;
            float y = startY + (i * lineHeight);

            font.drawInBatch(
                    line,
                    x,
                    y,
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

    }

    /**
     * Wrap by actual Minecraft font pixel width instead of raw character count.
     * Long words are continued onto the next line instead of being discarded.
     */
    private List<String> wrapToPixelWidth(String text, int maxPixelWidth, int maxLines) {
        List<String> result = new ArrayList<>();

        String remaining = (text == null || text.isBlank())
                ? "Unknown cause"
                : text.trim().replaceAll("\\s+", " ");

        while (!remaining.isEmpty() && result.size() < maxLines) {
            if (font.width(remaining) <= maxPixelWidth) {
                result.add(remaining);
                break;
            }

            int bestBreak = -1;
            int lastSpace = -1;

            // Find the longest prefix that physically fits on the stone.
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

            // Prefer wrapping at a word boundary. If one word itself is too
            // wide, split that word across lines instead of truncating it.
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

        // If the reason still does not fit, mark the final visible line.
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
