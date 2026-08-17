package hal.studios.hpm;

import java.util.function.Function;

import hal.studios.hpm.item.CannonWeaponItem;
import hal.studios.hpm.item.SpannerItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public final class HpmItems {
    private HpmItems() {}

    private static ResourceKey<Item> key(String name) {
        return ResourceKey.create(Registries.ITEM, HpmMod.id(name));
    }

    private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        ResourceKey<Item> key = key(name);
        Item item = factory.apply(properties.setId(key));
        Registry.register(BuiltInRegistries.ITEM, key, item);
        return item;
    }

    public static final Item CANNONBALL = register("cannonball", Item::new, new Item.Properties().stacksTo(64));
    public static final Item MORTAR_BALL = register("mortar_ball", Item::new, new Item.Properties().stacksTo(64));
    public static final Item SMALLHULL = register("smallhull", Item::new, new Item.Properties().stacksTo(64));
    public static final Item SMALL_MAST = register("small_mast", Item::new, new Item.Properties().stacksTo(64));
    public static final Item LARGEHULL = register("largehull", Item::new, new Item.Properties().stacksTo(64));
    public static final Item LARGEMAST = register("largemast", Item::new, new Item.Properties().stacksTo(64));

    public static final Item RAFTITEM = register("raftitem", p -> new BoatItem(HpmEntities.RAFT, p), new Item.Properties().stacksTo(1));
    public static final Item SWASHBUCKLERITEM = register("swashbuckleritem", p -> new BoatItem(HpmEntities.SWASHBUCKLER, p), new Item.Properties().stacksTo(1));
    public static final Item SWASHBUCKLERUPGRADE = register("swashbucklerupgrade", p -> new BoatItem(HpmEntities.SWASHBUCKLER_UPGRADED, p), new Item.Properties().stacksTo(1));
    public static final Item CUTTERITEM = register("cutteritem", p -> new BoatItem(HpmEntities.CUTTER, p), new Item.Properties().stacksTo(1));
    public static final Item CUTTERMILITARISEDITEM = register("cuttermilitariseditem", p -> new BoatItem(HpmEntities.CUTTER_MILITARISED, p), new Item.Properties().stacksTo(1));
    public static final Item PIRATE_CUTTER_ITEM = register("pirate_cutter_item", p -> new BoatItem(HpmEntities.CUTTER_PIRATE, p), new Item.Properties().stacksTo(1));
    public static final Item CORVETTE_STEAMSHIP_ITEM = register("corvette_steamship_item", p -> new BoatItem(HpmEntities.CORVETTE_STEAMSHIP, p), new Item.Properties().stacksTo(1));

    public static final Item SPANNER = register("spanner", SpannerItem::new, new Item.Properties().durability(50));
    public static final Item HAND_CANNON = register("hand_cannon", p -> new CannonWeaponItem(p, CANNONBALL, 1), new Item.Properties().durability(59));
    public static final Item HAND_MORTAR = register("hand_mortar", p -> new CannonWeaponItem(p, MORTAR_BALL, 3), new Item.Properties().durability(59));

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> {
            output.accept(RAFTITEM);
            output.accept(SWASHBUCKLERITEM);
            output.accept(SWASHBUCKLERUPGRADE);
            output.accept(CUTTERITEM);
            output.accept(CUTTERMILITARISEDITEM);
            output.accept(PIRATE_CUTTER_ITEM);
            output.accept(CORVETTE_STEAMSHIP_ITEM);
        });
    }
}
