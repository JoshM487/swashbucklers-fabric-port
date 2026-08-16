package hal.studios.hpm;

import hal.studios.hpm.entity.HpmShipEntity;
import hal.studios.hpm.entity.HpmShipSpec;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class HpmEntities {
    private HpmEntities() {}

    private static final HpmShipSpec RAFT_SPEC = new HpmShipSpec(0.3, 0.03, 0.45, 3.0f, 0.0, 16.0, 0.05, true);
    private static final HpmShipSpec SWASH_SPEC = new HpmShipSpec(0.3, 0.03, 0.60, 3.0f, -0.4, 50.0, 0.10, false);
    private static final HpmShipSpec UPGRADED_SPEC = new HpmShipSpec(0.3, 0.03, 0.70, 3.0f, -0.4, 80.0, 0.10, true);
    private static final HpmShipSpec CUTTER_SPEC = new HpmShipSpec(0.525, 0.05, 0.55, 3.0f, -0.5, 100.0, 0.10, true);
    private static final HpmShipSpec MIL_SPEC = new HpmShipSpec(0.525, 0.05, 0.65, 3.0f, -0.5, 120.0, 0.10, true);
    private static final HpmShipSpec PIRATE_SPEC = new HpmShipSpec(0.525, 0.05, 0.68, 3.0f, -0.5, 120.0, 0.10, true);
    private static final HpmShipSpec CORVETTE_SPEC = new HpmShipSpec(0.3, 0.03, 0.80, 1.0f, 0.0, 350.0, 0.10, false);

    private static EntityType<HpmShipEntity> register(String name, float width, float height, HpmShipSpec spec) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, HpmMod.id(name));
        EntityType<HpmShipEntity> type = EntityType.Builder.<HpmShipEntity>of(
                (entityType, level) -> new HpmShipEntity(entityType, level, spec), MobCategory.MISC)
                .sized(width, height)
                .clientTrackingRange(64)
                .updateInterval(3)
                .build(key);
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
    }

    public static final EntityType<HpmShipEntity> RAFT = register("raft", 2.0f, 0.4f, RAFT_SPEC);
    public static final EntityType<HpmShipEntity> SWASHBUCKLER = register("swashbuckler", 1.5f, 0.45f, SWASH_SPEC);
    public static final EntityType<HpmShipEntity> SWASHBUCKLER_UPGRADED = register("swashbucklerupgraded", 1.3f, 0.45f, UPGRADED_SPEC);
    public static final EntityType<HpmShipEntity> CUTTER = register("cutter", 1.6f, 0.7f, CUTTER_SPEC);
    public static final EntityType<HpmShipEntity> CUTTER_MILITARISED = register("cuttermilitarised", 1.6f, 0.7f, MIL_SPEC);
    public static final EntityType<HpmShipEntity> CUTTER_PIRATE = register("cutter_pirate", 1.6f, 0.7f, PIRATE_SPEC);
    public static final EntityType<HpmShipEntity> CORVETTE_STEAMSHIP = register("corvette_steamship", 4.0f, 1.3f, CORVETTE_SPEC);

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(RAFT, HpmShipEntity.createShipAttributes(RAFT_SPEC));
        FabricDefaultAttributeRegistry.register(SWASHBUCKLER, HpmShipEntity.createShipAttributes(SWASH_SPEC));
        FabricDefaultAttributeRegistry.register(SWASHBUCKLER_UPGRADED, HpmShipEntity.createShipAttributes(UPGRADED_SPEC));
        FabricDefaultAttributeRegistry.register(CUTTER, HpmShipEntity.createShipAttributes(CUTTER_SPEC));
        FabricDefaultAttributeRegistry.register(CUTTER_MILITARISED, HpmShipEntity.createShipAttributes(MIL_SPEC));
        FabricDefaultAttributeRegistry.register(CUTTER_PIRATE, HpmShipEntity.createShipAttributes(PIRATE_SPEC));
        FabricDefaultAttributeRegistry.register(CORVETTE_STEAMSHIP, HpmShipEntity.createShipAttributes(CORVETTE_SPEC));
    }
}
