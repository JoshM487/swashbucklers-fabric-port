package hal.studios.hpm;

import java.util.function.Supplier;

import hal.studios.hpm.entity.HpmBoat;
import hal.studios.hpm.entity.HpmChestBoat;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.item.Item;

public final class HpmEntities {
    private HpmEntities() {}

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, HpmMod.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    private static EntityType<Boat> boat(String name, float width, float height, Supplier<Item> dropItem,
            double buoyancyProbeYOffset, double buoyancyLiftVelocity,
            double maxSpeed, float turnDegreesPerTick, double passengerAttachmentY) {
        return register(name, EntityType.Builder.<Boat>of(
                (type, level) -> new HpmBoat(type, level, dropItem,
                        buoyancyProbeYOffset, buoyancyLiftVelocity,
                        maxSpeed, turnDegreesPerTick, passengerAttachmentY),
                MobCategory.MISC)
                .sized(width, height)
                .clientTrackingRange(64)
                .updateInterval(3));
    }

    private static EntityType<ChestBoat> chestBoat(String name, float width, float height, Supplier<Item> dropItem,
            double buoyancyProbeYOffset, double buoyancyLiftVelocity,
            double maxSpeed, float turnDegreesPerTick, double passengerAttachmentY) {
        return register(name, EntityType.Builder.<ChestBoat>of(
                (type, level) -> new HpmChestBoat(type, level, dropItem,
                        buoyancyProbeYOffset, buoyancyLiftVelocity,
                        maxSpeed, turnDegreesPerTick, passengerAttachmentY),
                MobCategory.MISC)
                .sized(width, height)
                .clientTrackingRange(64)
                .updateInterval(3));
    }

    /*
     * The original PathfinderMob entities used +0.30 / +0.525 water probes.
     * On the 26.1 Boat compatibility shell those offsets make the much larger
     * recovered hull models settle visibly too low. A 0.0 probe raises the
     * six non-corvette ships by roughly 0.30-0.525 blocks while preserving the
     * same original lift velocities. The Corvette already has the correct
     * visual waterline and intentionally keeps its +0.30 probe.
     */
    public static final EntityType<ChestBoat> RAFT = chestBoat(
            "raft", 2.0f, 0.4f, () -> HpmItems.RAFTITEM,
            0.0, 0.03, 0.45, 3.0f, 0.40);

    public static final EntityType<Boat> SWASHBUCKLER = boat(
            "swashbuckler", 1.5f, 0.45f, () -> HpmItems.SWASHBUCKLERITEM,
            0.0, 0.03, 0.60, 3.0f, 0.05);

    public static final EntityType<ChestBoat> SWASHBUCKLER_UPGRADED = chestBoat(
            "swashbucklerupgraded", 1.3f, 0.45f, () -> HpmItems.SWASHBUCKLERUPGRADE,
            0.0, 0.03, 0.70, 3.0f, 0.05);

    public static final EntityType<ChestBoat> CUTTER = chestBoat(
            "cutter", 1.6f, 0.7f, () -> HpmItems.CUTTERITEM,
            0.0, 0.05, 0.55, 3.0f, 0.20);

    public static final EntityType<ChestBoat> CUTTER_MILITARISED = chestBoat(
            "cuttermilitarised", 1.6f, 0.7f, () -> HpmItems.CUTTERMILITARISEDITEM,
            0.0, 0.05, 0.65, 3.0f, 0.20);

    public static final EntityType<ChestBoat> CUTTER_PIRATE = chestBoat(
            "cutter_pirate", 1.6f, 0.7f, () -> HpmItems.PIRATE_CUTTER_ITEM,
            0.0, 0.05, 0.68, 3.0f, 0.20);

    public static final EntityType<Boat> CORVETTE_STEAMSHIP = boat(
            "corvette_steamship", 4.0f, 1.3f, () -> HpmItems.CORVETTE_STEAMSHIP_ITEM,
            // Real-client calibration: 1.30 was visibly too high; 0.70 produced
            // playerY-shipY ~= 0.10 and put the rider into the hull. 0.95 targets
            // the visible deck instead of either extreme.
            0.3, 0.03, 0.80, 1.0f, 0.95);

    public static void initialize() {}
}
