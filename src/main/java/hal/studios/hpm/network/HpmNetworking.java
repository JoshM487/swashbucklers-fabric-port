package hal.studios.hpm.network;

import hal.studios.hpm.entity.HpmShipEntity;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.Entity;

public final class HpmNetworking {
    private HpmNetworking() {}

    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(HpmShipInputPayload.TYPE, HpmShipInputPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(HpmShipInputPayload.TYPE, (payload, context) -> {
            Entity vehicle = context.player().getVehicle();
            if (!(vehicle instanceof HpmShipEntity ship) || ship.getId() != payload.entityId()) return;
            int flags = payload.flags();
            ship.hpm$setOriginalInput((flags & 1) != 0, (flags & 2) != 0, (flags & 4) != 0, (flags & 8) != 0);
        });
    }
}
