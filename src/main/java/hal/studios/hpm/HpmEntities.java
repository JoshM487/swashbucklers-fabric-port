package hal.studios.hpm;

import java.util.function.Supplier;

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

    private static EntityType<Boat> boat(String name, float width, float height, Supplier<Item> dropItem) {
        return register(name, EntityType.Builder.<Boat>of(
                (type, level) -> new Boat(type, level, dropItem), MobCategory.MISC)
                .sized(width, height));
    }

    private static EntityType<ChestBoat> chestBoat(String name, float width, float height, Supplier<Item> dropItem) {
        return register(name, EntityType.Builder.<ChestBoat>of(
                (type, level) -> new ChestBoat(type, level, dropItem), MobCategory.MISC)
                .sized(width, height));
    }

    public static final EntityType<Boat> RAFT = boat("raft", 2.0f, 0.4f, () -> HpmItems.RAFTITEM);
    public static final EntityType<Boat> SWASHBUCKLER = boat("swashbuckler", 1.5f, 0.45f, () -> HpmItems.SWASHBUCKLERITEM);
    public static final EntityType<ChestBoat> SWASHBUCKLER_UPGRADED = chestBoat("swashbucklerupgraded", 1.3f, 0.45f, () -> HpmItems.SWASHBUCKLERUPGRADE);
    public static final EntityType<ChestBoat> CUTTER = chestBoat("cutter", 1.6f, 0.7f, () -> HpmItems.CUTTERITEM);
    public static final EntityType<ChestBoat> CUTTER_MILITARISED = chestBoat("cuttermilitarised", 1.6f, 0.7f, () -> HpmItems.CUTTERMILITARISEDITEM);
    public static final EntityType<ChestBoat> CUTTER_PIRATE = chestBoat("cutter_pirate", 1.6f, 0.7f, () -> HpmItems.PIRATE_CUTTER_ITEM);
    public static final EntityType<ChestBoat> CORVETTE_STEAMSHIP = chestBoat("corvette_steamship", 4.0f, 1.3f, () -> HpmItems.CORVETTE_STEAMSHIP_ITEM);

    public static void initialize() {}
}
