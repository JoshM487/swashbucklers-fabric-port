package hal.studios.hpm.client.renderer;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.blaze3d.vertex.PoseStack;
import hal.studios.hpm.client.model.HpmShipModel;
import hal.studios.hpm.entity.HpmShipEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

public final class HpmShipRenderer extends MobRenderer<HpmShipEntity, LivingEntityRenderState, HpmShipModel> {
    private static final Set<String> CI_RENDERED_IDS = ConcurrentHashMap.newKeySet();
    private final Identifier texture;
    private final float modelScale;
    private final String debugId;

    public HpmShipRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer, Identifier texture,
            float modelScale, float shadowRadius, String debugId) {
        super(context, new HpmShipModel(context.bakeLayer(layer)), shadowRadius);
        this.texture = texture;
        this.modelScale = modelScale;
        this.debugId = debugId;
    }

    @Override
    public LivingEntityRenderState createRenderState() { return new LivingEntityRenderState(); }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) { return this.texture; }

    @Override
    public void submit(LivingEntityRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraState) {
        poseStack.pushPose();
        poseStack.scale(this.modelScale, this.modelScale, this.modelScale);
        super.submit(state, poseStack, nodeCollector, cameraState);
        poseStack.popPose();
        if (Boolean.getBoolean("hpm.ci.renderTest") && CI_RENDERED_IDS.add(this.debugId)) {
            System.out.println("SWASHBUCKLERS_RENDER_OK " + this.debugId);
        }
    }

    public static void resetCiRenderProbe() { CI_RENDERED_IDS.clear(); }
    public static boolean ciHasRenderedAll(Set<String> expectedIds) { return CI_RENDERED_IDS.containsAll(expectedIds); }
    public static Set<String> ciRenderedIds() { return Set.copyOf(CI_RENDERED_IDS); }
}
