package hal.studios.hpm.gametest;

import java.util.Set;

import hal.studios.hpm.client.renderer.HpmShipRenderer;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

@SuppressWarnings("UnstableApiUsage")
public final class HpmClientRenderGameTest implements FabricClientGameTest {
    private static final Set<String> SHIP_IDS = Set.of(
        "hpm:raft",
        "hpm:swashbuckler",
        "hpm:swashbucklerupgraded",
        "hpm:cutter",
        "hpm:cuttermilitarised",
        "hpm:cutter_pirate",
        "hpm:corvette_steamship"
    );

    @Override
    public void runTest(ClientGameTestContext context) {
        HpmShipRenderer.resetCiRenderProbe();

        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            TestServerContext server = singleplayer.getServer();

            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("fill -16 63 -4 16 63 28 minecraft:stone");
            server.runCommand("tp @a 0 65 0 0 12");

            String[] ships = {
                "hpm:raft",
                "hpm:swashbuckler",
                "hpm:swashbucklerupgraded",
                "hpm:cutter",
                "hpm:cuttermilitarised",
                "hpm:cutter_pirate",
                "hpm:corvette_steamship"
            };
            int[] xs = {-9, -6, -3, 0, 3, 6, 9};

            for (int i = 0; i < ships.length; i++) {
                server.runCommand("summon " + ships[i] + " " + xs[i] + " 64 18");
            }

            // Let server spawns sync to the client, then wait until the surrounding chunks are actually drawn.
            context.waitTicks(20);
            singleplayer.getClientLevel().waitForChunksRender();

            // submit(...) runs on real client render frames. This predicate cannot succeed from model baking alone.
            context.waitFor(client -> HpmShipRenderer.ciHasRenderedAll(SHIP_IDS), 30 * 20);
            context.takeScreenshot("swashbucklers-seven-ships-rendered");

            System.out.println("SWASHBUCKLERS_CLIENT_RENDER_TEST_OK " + HpmShipRenderer.ciRenderedIds());
        }
    }
}
