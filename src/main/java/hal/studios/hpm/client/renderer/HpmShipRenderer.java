package hal.studios.hpm.client.renderer;

import hal.studios.hpm.client.model.HpmShipModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.resources.Identifier;

public final class HpmShipRenderer extends AbstractBoatRenderer {
    private final EntityModel<BoatRenderState> shipModel;

    public HpmShipRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer, Identifier texture) {
        super(context, texture);
        this.shipModel = new HpmShipModel(context.bakeLayer(layer));
    }

    @Override
    protected EntityModel<BoatRenderState> model() {
        return this.shipModel;
    }
}
