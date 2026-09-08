package rem.coffenmod.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Direct Forge 1.20.1 translation of the original 1.7.10 ModelCoffin.
 *
 * Important: the lid children intentionally keep the SAME local rotation points
 * used by the old ModelRenderer hierarchy.  They must not be offset again by
 * the lid parent position; doing so makes the lid explode away from the base.
 */
public class CoffinModel {
    public final ModelPart root;
    public final ModelPart lid;

    public CoffinModel(ModelPart root) {
        this.root = root;
        this.lid = root.getChild("lid");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Original Shape1..Shape6.
        root.addOrReplaceChild("shape1",
                CubeListBuilder.create().texOffs(116, 0)
                        .addBox(0.0F, 0.0F, 0.0F, 5.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("shape2",
                CubeListBuilder.create().texOffs(36, 14).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1.0F, 4.0F, 14.0F),
                PartPose.offsetAndRotation(4.0F, 0.0F, 0.1F, 0.0F, 0.158825F, 0.0F));

        root.addOrReplaceChild("shape3",
                CubeListBuilder.create().texOffs(36, 14)
                        .addBox(0.0F, 0.0F, 0.0F, 1.0F, 4.0F, 14.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.158825F, 0.0F));

        root.addOrReplaceChild("shape4",
                CubeListBuilder.create().texOffs(0, 28)
                        .addBox(0.0F, 0.0F, 0.0F, 1.0F, 4.0F, 8.0F),
                PartPose.offsetAndRotation(-2.2F, 0.0F, 13.8F, 0.0F, 0.1518436F, 0.0F));

        root.addOrReplaceChild("shape5",
                CubeListBuilder.create().texOffs(0, 28).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1.0F, 4.0F, 8.0F),
                PartPose.offsetAndRotation(6.2F, 0.0F, 13.65F, 0.0F, -0.1518436F, 0.0F));

        root.addOrReplaceChild("shape6",
                CubeListBuilder.create().texOffs(50, 26)
                        .addBox(0.0F, 0.0F, 0.0F, 7.0F, 4.0F, 1.0F),
                PartPose.offset(-1.0F, 0.0F, 20.7F));

        // The original model used two coincident zero-thickness floor planes,
        // each mapped to a different area of the 128x128 coffin texture.
        root.addOrReplaceChild("bottom",
                CubeListBuilder.create().texOffs(-22, 0)
                        .addBox(0.0F, 0.0F, 0.0F, 11.0F, 0.0F, 24.0F),
                PartPose.offset(-3.0F, 4.1F, -1.0F));

        root.addOrReplaceChild("bottom2",
                CubeListBuilder.create().texOffs(-22, 83)
                        .addBox(0.0F, 0.0F, 0.0F, 11.0F, 0.0F, 24.0F),
                PartPose.offset(-3.0F, 4.2F, -1.0F));

        // Original Shape15 is both a cube AND the moving parent for Shape7,
        // Shape8, Shape9, Shape12, Shape13 and Shape14.
        PartDefinition lid = root.addOrReplaceChild("lid",
                CubeListBuilder.create().texOffs(100, 20)
                        .addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 6.0F),
                PartPose.offset(-3.0F, -1.0F, 11.0F));

        lid.addOrReplaceChild("shape14",
                CubeListBuilder.create().texOffs(100, 0)
                        .addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 6.0F),
                PartPose.offset(10.0F, 0.0F, 0.0F));

        lid.addOrReplaceChild("shape13",
                CubeListBuilder.create().texOffs(94, 111)
                        .addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 16.0F),
                PartPose.offset(1.0F, 0.0F, -6.0F));

        lid.addOrReplaceChild("shape12",
                CubeListBuilder.create().texOffs(0, 111)
                        .addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 16.0F),
                PartPose.offset(9.0F, 0.0F, -6.0F));

        lid.addOrReplaceChild("shape9",
                CubeListBuilder.create().texOffs(82, 75)
                        .addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 22.0F),
                PartPose.offset(2.0F, 0.0F, -11.0F));

        lid.addOrReplaceChild("shape8",
                CubeListBuilder.create().texOffs(82, 50)
                        .addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 22.0F),
                PartPose.offset(8.0F, 0.0F, -11.0F));

        lid.addOrReplaceChild("shape7",
                CubeListBuilder.create().texOffs(0, 39)
                        .addBox(0.0F, 0.0F, 0.0F, 5.0F, 1.0F, 24.0F),
                PartPose.offset(3.0F, 0.0F, -12.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }
}
