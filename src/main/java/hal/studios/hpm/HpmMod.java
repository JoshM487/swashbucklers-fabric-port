package hal.studios.hpm;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

public final class HpmMod implements ModInitializer {
    public static final String MOD_ID = "hpm";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        HpmEntities.initialize();
        HpmItems.initialize();
    }
}
