package hal.studios.hpm.gametest;

import java.util.Set;

import hal.studios.hpm.client.renderer.HpmShipRenderer;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

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

            context.waitTicks(20);
            singleplayer.getClientLevel().waitForChunksRender();
            context.waitFor(client -> HpmShipRenderer.ciHasRenderedAll(SHIP_IDS), 30 * 20);
            context.takeScreenshot("swashbucklers-seven-ships-rendered");
            System.out.println("SWASHBUCKLERS_CLIENT_RENDER_TEST_OK " + HpmShipRenderer.ciRenderedIds());

            // Reproduce real gameplay in a large open-water course so the raft cannot run out
            // of the pool before we sample its position.
            server.runCommand("kill @e[type=#minecraft:boat]");
            server.runCommand("fill -40 62 -40 40 62 40 minecraft:stone");
            server.runCommand("fill -40 63 -40 40 64 40 minecraft:water");
            server.runCommand("tp @a 0 65 0 0 0");
            server.runCommand("summon hpm:raft 0 64 4 {Tags:[\"hpm_ci_control\"]}");
            context.waitTicks(10);
            server.runCommand("ride @p mount @e[type=hpm:raft,tag=hpm_ci_control,limit=1]");
            context.waitTicks(10);

            Vec3 start = context.computeOnClient(client -> {
                if (client.player == null) {
                    throw new AssertionError("Client player was unavailable during ship control test");
                }
                Entity vehicle = client.player.getVehicle();
                if (vehicle == null) {
                    throw new AssertionError("Player could not mount hpm:raft");
                }
                return vehicle.position();
            });

            // 30 ticks is enough for a vanilla-controlled boat to travel well over one block,
            // while keeping the raft safely inside the 81x81 test pool.
            context.getInput().holdKeyFor(options -> options.keyUp, 30);
            context.waitTicks(2);

            Vec3 end = context.computeOnClient(client -> {
                if (client.player == null || client.player.getVehicle() == null) {
                    throw new AssertionError("Player was dismounted during short open-water ship control test");
                }
                return client.player.getVehicle().position();
            });

            double horizontalDistance = Math.hypot(end.x - start.x, end.z - start.z);
            if (horizontalDistance < 1.0) {
                throw new AssertionError("Mounted hpm:raft did not respond to Forward input; moved only " + horizontalDistance + " blocks");
            }

            System.out.println("SWASHBUCKLERS_CONTROL_OK hpm:raft distance=" + horizontalDistance);
            context.takeScreenshot("swashbucklers-raft-forward-control");
        }
    }
}
