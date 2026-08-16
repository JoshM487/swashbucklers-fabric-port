package hal.studios.hpm.client;

import hal.studios.hpm.HpmEntities;
import hal.studios.hpm.HpmMod;
import hal.studios.hpm.client.model.HpmShipModelLayers;
import hal.studios.hpm.client.renderer.HpmShipRenderer;
import hal.studios.hpm.entity.HpmShipEntity;
import hal.studios.hpm.network.HpmShipInputPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

public final class HpmClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HpmShipModelLayers.register();
        register(HpmEntities.RAFT, HpmShipModelLayers.RAFT, "raft.png", 1.9f, 1.7f, "hpm:raft");
        register(HpmEntities.SWASHBUCKLER, HpmShipModelLayers.SWASHBUCKLER, "swashbuckler.png", 3.5f, 1.8f, "hpm:swashbuckler");
        register(HpmEntities.SWASHBUCKLER_UPGRADED, HpmShipModelLayers.SWASHBUCKLER_UPGRADED, "swashbucklerupgraded.png", 5.0f, 1.8f, "hpm:swashbucklerupgraded");
        register(HpmEntities.CUTTER, HpmShipModelLayers.CUTTER, "cutterremastered.png", 5.0f, 2.5f, "hpm:cutter");
        register(HpmEntities.CUTTER_MILITARISED, HpmShipModelLayers.CUTTER_WEAPONISED, "cutterweaponisedremastered.png", 5.0f, 2.5f, "hpm:cuttermilitarised");
        register(HpmEntities.CUTTER_PIRATE, HpmShipModelLayers.CUTTER_WEAPONISED, "cutterpirateremastered.png", 5.0f, 2.5f, "hpm:cutter_pirate");
        register(HpmEntities.CORVETTE_STEAMSHIP, HpmShipModelLayers.CORVETTE, "corvetteclass.png", 4.0f, 2.5f, "hpm:corvette_steamship");

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || !(client.player.getVehicle() instanceof HpmShipEntity ship)) return;
            int flags = 0;
            if (client.options.keyUp.isDown()) flags |= 1;
            if (client.options.keyDown.isDown()) flags |= 2;
            if (client.options.keyLeft.isDown()) flags |= 4;
            if (client.options.keyRight.isDown()) flags |= 8;
            if (ClientPlayNetworking.canSend(HpmShipInputPayload.TYPE)) {
                ClientPlayNetworking.send(new HpmShipInputPayload(ship.getId(), flags));
            }
        });
    }

    private static void register(EntityType<HpmShipEntity> type, ModelLayerLocation layer, String textureName,
            float scale, float shadow, String debugId) {
        Identifier texture = HpmMod.id("textures/entities/" + textureName);
        EntityRenderers.register(type, context -> new HpmShipRenderer(context, layer, texture, scale, shadow, debugId));
    }
}
