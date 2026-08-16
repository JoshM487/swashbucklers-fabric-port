package hal.studios.hpm.client;

import hal.studios.hpm.HpmEntities;
import hal.studios.hpm.HpmMod;
import hal.studios.hpm.client.model.HpmShipModelLayers;
import hal.studios.hpm.client.renderer.HpmShipRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

public final class HpmClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HpmShipModelLayers.register();
        register(HpmEntities.RAFT, HpmShipModelLayers.RAFT, "raft.png");
        register(HpmEntities.SWASHBUCKLER, HpmShipModelLayers.SWASHBUCKLER, "swashbuckler.png");
        register(HpmEntities.SWASHBUCKLER_UPGRADED, HpmShipModelLayers.SWASHBUCKLER_UPGRADED, "swashbucklerupgraded.png");
        register(HpmEntities.CUTTER, HpmShipModelLayers.CUTTER, "cutterremastered.png");
        register(HpmEntities.CUTTER_MILITARISED, HpmShipModelLayers.CUTTER_WEAPONISED, "cutterweaponisedremastered.png");
        register(HpmEntities.CUTTER_PIRATE, HpmShipModelLayers.CUTTER_WEAPONISED, "cutterpirateremastered.png");
        register(HpmEntities.CORVETTE_STEAMSHIP, HpmShipModelLayers.CORVETTE, "corvetteclass.png");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void register(EntityType<? extends AbstractBoat> type, ModelLayerLocation layer, String textureName) {
        Identifier texture = HpmMod.id("textures/entities/" + textureName);
        EntityRenderers.register((EntityType) type, context -> new HpmShipRenderer(context, layer, texture));
    }
}
