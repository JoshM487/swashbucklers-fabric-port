package hal.studios.hpm.client.model;

import java.lang.reflect.InvocationTargetException;

import hal.studios.hpm.HpmMod;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public final class HpmShipModelLayers {
    private HpmShipModelLayers() {}

    public static final ModelLayerLocation RAFT = main("modelraft");
    public static final ModelLayerLocation SWASHBUCKLER = main("modelswashbuckler");
    public static final ModelLayerLocation SWASHBUCKLER_UPGRADED = main("modelswashbucklerupgraded");
    public static final ModelLayerLocation CUTTER = main("modelcutterremastered");
    public static final ModelLayerLocation CUTTER_WEAPONISED = main("modelcutterweaponisedremastered");
    public static final ModelLayerLocation CORVETTE = main("modelcorvetteclass");

    private static ModelLayerLocation main(String name) {
        return new ModelLayerLocation(HpmMod.id(name), "main");
    }

    public static void register() {
        register(RAFT, "Modelraft");
        register(SWASHBUCKLER, "Modelswashbuckler");
        register(SWASHBUCKLER_UPGRADED, "Modelswashbucklerupgraded");
        register(CUTTER, "Modelcutterremastered");
        register(CUTTER_WEAPONISED, "Modelcutterweaponisedremastered");
        register(CORVETTE, "Modelcorvetteclass");
    }

    private static void register(ModelLayerLocation layer, String modelClass) {
        ModelLayerRegistry.registerModelLayer(layer, () -> createBodyLayer(modelClass));
    }

    private static LayerDefinition createBodyLayer(String simpleName) {
        String name = "hal.studios.hpm.client.model." + simpleName;
        try {
            Class<?> clazz = Class.forName(name);
            return (LayerDefinition) clazz.getMethod("createBodyLayer").invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to load original Swashbucklers model " + name, e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new IllegalStateException("Original Swashbucklers model failed to bake: " + name, cause);
        }
    }
}
