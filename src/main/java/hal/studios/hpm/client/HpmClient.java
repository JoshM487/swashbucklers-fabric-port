package hal.studios.hpm.client;

import hal.studios.hpm.HpmEntities;
import hal.studios.hpm.HpmMod;
import hal.studios.hpm.client.model.HpmShipModelLayers;
import hal.studios.hpm.client.renderer.HpmShipRenderer;
import hal.studios.hpm.entity.HpmControllableShip;
import hal.studios.hpm.network.HpmShipInputPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

public final class HpmClient implements ClientModInitializer {
    private static final double NON_CORVETTE_WATERLINE_OFFSET = 0.75D;

    @Override
    public void onInitializeClient() {
        HpmShipModelLayers.register();
        register(HpmEntities.RAFT, HpmShipModelLayers.RAFT, "raft.png", "hpm:raft", NON_CORVETTE_WATERLINE_OFFSET);
        register(HpmEntities.SWASHBUCKLER, HpmShipModelLayers.SWASHBUCKLER, "swashbuckler.png", "hpm:swashbuckler", NON_CORVETTE_WATERLINE_OFFSET);
        register(HpmEntities.SWASHBUCKLER_UPGRADED, HpmShipModelLayers.SWASHBUCKLER_UPGRADED, "swashbucklerupgraded.png", "hpm:swashbucklerupgraded", NON_CORVETTE_WATERLINE_OFFSET);
        register(HpmEntities.CUTTER, HpmShipModelLayers.CUTTER, "cutterremastered.png", "hpm:cutter", NON_CORVETTE_WATERLINE_OFFSET);
        register(HpmEntities.CUTTER_MILITARISED, HpmShipModelLayers.CUTTER_WEAPONISED, "cutterweaponisedremastered.png", "hpm:cuttermilitarised", NON_CORVETTE_WATERLINE_OFFSET);
        register(HpmEntities.CUTTER_PIRATE, HpmShipModelLayers.CUTTER_WEAPONISED, "cutterpirateremastered.png", "hpm:cutter_pirate", NON_CORVETTE_WATERLINE_OFFSET);
        register(HpmEntities.CORVETTE_STEAMSHIP, HpmShipModelLayers.CORVETTE, "corvetteclass.png", "hpm:corvette_steamship", 0.0D);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            Entity vehicle = client.player.getVehicle();
            if (!(vehicle instanceof AbstractBoat) || !(vehicle instanceof HpmControllableShip ship)) return;

            int flags = 0;
            // User-facing controls are intentionally inverted from the previous build:
            // S raises sail/throttle, W lowers sail/throttle and continues into reverse.
            if (client.options.keyUp.isDown()) flags |= 2;
            if (client.options.keyDown.isDown()) flags |= 1;
            if (client.options.keyLeft.isDown()) flags |= 4;
            if (client.options.keyRight.isDown()) flags |= 8;

            ship.hpm$setOriginalInput(
                    (flags & 1) != 0,
                    (flags & 2) != 0,
                    (flags & 4) != 0,
                    (flags & 8) != 0);

            if (ClientPlayNetworking.canSend(HpmShipInputPayload.TYPE)) {
                ClientPlayNetworking.send(new HpmShipInputPayload(vehicle.getId(), flags));
            }
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void register(EntityType<? extends AbstractBoat> type, ModelLayerLocation layer,
            String textureName, String debugId, double verticalOffset) {
        Identifier texture = HpmMod.id("textures/entities/" + textureName);
        EntityRenderers.register((EntityType) type,
                context -> new HpmShipRenderer(context, layer, texture, debugId, verticalOffset));
    }
}
