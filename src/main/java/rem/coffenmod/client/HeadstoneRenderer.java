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
         * PlayerName
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
        lines.add(be.getPlayerName().isBlank() ? "Unknown" : be.getPlayerName());

        // 88 font pixels fits nicely across this wider headstone.
        lines.addAll(wrapToPixelWidth(
                be.getDeathReason(),
                88,
                4
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
     * This keeps every line physically inside the headstone face.
     */
    private List<String> wrapToPixelWidth(String text, int maxPixelWidth, int maxLines) {
        List<String> result = new ArrayList<>();

        String remaining = (text == null || text.isBlank())
                ? "Unknown cause"
                : text.trim();

        while (!remaining.isEmpty() && result.size() < maxLines) {

            // If the rest already fits, use it as the final line.
            if (font.width(remaining) <= maxPixelWidth) {
                result.add(remaining);
                remaining = "";
                break;
            }

            String[] words = remaining.split("\\s+");
            StringBuilder line = new StringBuilder();
            int consumedWords = 0;

            for (String word : words) {
                String candidate = line.length() == 0
                        ? word
                        : line + " " + word;

                if (font.width(candidate) > maxPixelWidth) {
                    break;
                }

                line.setLength(0);
                line.append(candidate);
                consumedWords++;
            }

            // Very long single word: trim it until it fits.
            if (consumedWords == 0) {
                String word = words[0];
                String fitted = word;

                while (!fitted.isEmpty() && font.width(fitted + "...") > maxPixelWidth) {
                    fitted = fitted.substring(0, fitted.length() - 1);
                }

                result.add(fitted + "...");

                remaining = remaining.substring(
                        Math.min(word.length(), remaining.length())
                ).trim();
                continue;
            }

            result.add(line.toString());

            // Remove the words we just consumed.
            StringBuilder rest = new StringBuilder();
            for (int i = consumedWords; i < words.length; i++) {
                if (rest.length() > 0) rest.append(' ');
                rest.append(words[i]);
            }

            remaining = rest.toString();
        }

        // If text remains after maxLines, add an ellipsis to the last line.
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
