package hal.studios.hpm.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import hal.studios.hpm.client.model.HpmShipModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.resources.Identifier;

public final class HpmShipRenderer extends AbstractBoatRenderer {
    private final EntityModel<BoatRenderState> shipModel;
    private final String debugId;
    private boolean renderLogged;

    public HpmShipRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer, Identifier texture, String debugId) {
        super(context, texture);
        this.shipModel = new HpmShipModel(context.bakeLayer(layer));
        this.debugId = debugId;
    }

    @Override
    protected EntityModel<BoatRenderState> model() {
        return this.shipModel;
    }

    @Override
    public void render(BoatRenderState state, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(state, poseStack, bufferSource, packedLight);

        if (Boolean.getBoolean("hpm.ci.renderTest") && !this.renderLogged) {
            this.renderLogged = true;
            System.out.println("SWASHBUCKLERS_RENDER_OK " + this.debugId);
        }
    }
}
