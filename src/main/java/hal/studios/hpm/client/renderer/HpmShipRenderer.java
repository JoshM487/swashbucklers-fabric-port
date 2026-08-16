package hal.studios.hpm.client.renderer;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.blaze3d.vertex.PoseStack;

import hal.studios.hpm.client.model.HpmShipModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

public final class HpmShipRenderer extends AbstractBoatRenderer {
    private static final Set<String> CI_RENDERED_IDS = ConcurrentHashMap.newKeySet();

    private final EntityModel<BoatRenderState> shipModel;
    private final String debugId;

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
    public void submit(BoatRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraState) {
        super.submit(state, poseStack, nodeCollector, cameraState);

        if (Boolean.getBoolean("hpm.ci.renderTest") && CI_RENDERED_IDS.add(this.debugId)) {
            System.out.println("SWASHBUCKLERS_RENDER_OK " + this.debugId);
        }
    }

    public static void resetCiRenderProbe() {
        CI_RENDERED_IDS.clear();
    }

    public static boolean ciHasRenderedAll(Set<String> expectedIds) {
        return CI_RENDERED_IDS.containsAll(expectedIds);
    }

    public static Set<String> ciRenderedIds() {
        return Set.copyOf(CI_RENDERED_IDS);
    }
}
