package hal.studios.hpm.gametest;

import java.util.Set;

import hal.studios.hpm.client.renderer.HpmShipRenderer;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
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

            // Let server spawns sync to the client, then wait until the surrounding chunks are actually drawn.
            context.waitTicks(20);
            singleplayer.getClientLevel().waitForChunksRender();

            // submit(...) runs on real client render frames. This predicate cannot succeed from model baking alone.
            context.waitFor(client -> HpmShipRenderer.ciHasRenderedAll(SHIP_IDS), 30 * 20);
            context.takeScreenshot("swashbucklers-seven-ships-rendered");
            System.out.println("SWASHBUCKLERS_CLIENT_RENDER_TEST_OK " + HpmShipRenderer.ciRenderedIds());

            // Now reproduce real gameplay: put a fresh raft on water, mount it, hold the real
            // client Forward key, and require the ridden entity to move horizontally.
            server.runCommand("kill @e[type=#minecraft:boat]");
            server.runCommand("fill -12 62 -12 12 62 18 minecraft:stone");
            server.runCommand("fill -12 63 -12 12 64 18 minecraft:water");
            server.runCommand("tp @a 0 65 0 0 0");
            server.runCommand("summon hpm:raft 0 64 4 {Tags:[\"hpm_ci_control\"]}");
            context.waitTicks(10);
            server.runCommand("ride @a mount @e[type=hpm:raft,tag=hpm_ci_control,limit=1]");
            context.waitTicks(10);

            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) {
                throw new AssertionError("Client player was unavailable during ship control test");
            }

            Entity vehicle = minecraft.player.getVehicle();
            if (vehicle == null) {
                throw new AssertionError("Player could not mount hpm:raft");
            }

            Vec3 start = vehicle.position();
            try {
                minecraft.options.keyUp.setDown(true);
                context.waitTicks(80);
            } finally {
                minecraft.options.keyUp.setDown(false);
            }

            Entity movedVehicle = minecraft.player.getVehicle();
            if (movedVehicle == null) {
                throw new AssertionError("Player was dismounted during ship control test");
            }

            Vec3 end = movedVehicle.position();
            double horizontalDistance = Math.hypot(end.x - start.x, end.z - start.z);
            if (horizontalDistance < 1.0) {
                throw new AssertionError("Mounted hpm:raft did not respond to Forward input; moved only " + horizontalDistance + " blocks");
            }

            System.out.println("SWASHBUCKLERS_CONTROL_OK hpm:raft distance=" + horizontalDistance);
            context.takeScreenshot("swashbucklers-raft-forward-control");
        }
    }
}
