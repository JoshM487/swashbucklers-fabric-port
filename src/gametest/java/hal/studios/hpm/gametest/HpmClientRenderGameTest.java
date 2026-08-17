package hal.studios.hpm.gametest;

import java.util.Set;

import hal.studios.hpm.client.renderer.HpmShipRenderer;
import hal.studios.hpm.entity.HpmControllableShip;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("UnstableApiUsage")
public final class HpmClientRenderGameTest implements FabricClientGameTest {
    private static final Set<String> SHIP_IDS = Set.of("hpm:raft", "hpm:swashbuckler", "hpm:swashbucklerupgraded", "hpm:cutter", "hpm:cuttermilitarised", "hpm:cutter_pirate", "hpm:corvette_steamship");

    @Override
    public void runTest(ClientGameTestContext context) {
        HpmShipRenderer.resetCiRenderProbe();
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            TestServerContext server = singleplayer.getServer();
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("fill -16 63 -4 16 63 28 minecraft:stone");
            server.runCommand("tp @a 0 65 0 0 12");
            String[] ships = {"hpm:raft","hpm:swashbuckler","hpm:swashbucklerupgraded","hpm:cutter","hpm:cuttermilitarised","hpm:cutter_pirate","hpm:corvette_steamship"};
            int[] xs = {-9,-6,-3,0,3,6,9};
            for (int i=0;i<ships.length;i++) server.runCommand("summon " + ships[i] + " " + xs[i] + " 64 18");
            context.waitTicks(20);
            singleplayer.getClientLevel().waitForChunksRender();
            context.waitFor(client -> HpmShipRenderer.ciHasRenderedAll(SHIP_IDS), 30 * 20);
            context.takeScreenshot("swashbucklers-seven-ships-visible");
            System.out.println("SWASHBUCKLERS_CLIENT_RENDER_TEST_OK " + HpmShipRenderer.ciRenderedIds());

            server.runCommand("kill @e[type=#minecraft:boat]");
            server.runCommand("fill -50 62 -50 50 62 50 minecraft:stone");
            server.runCommand("fill -50 63 -50 50 64 50 minecraft:water");
            server.runCommand("tp @a 0 65 0 90 0");
            server.runCommand("summon hpm:raft 0 64 4 {Rotation:[0f,0f],Tags:[\"hpm_ci_control\"]}");
            context.waitTicks(10);
            server.runCommand("ride @p mount @e[type=hpm:raft,tag=hpm_ci_control,limit=1]");
            context.waitTicks(10);

            Vec3 start = context.computeOnClient(client -> {
                if (client.player == null || !(client.player.getVehicle() instanceof AbstractBoat vehicle)) throw new AssertionError("Player could not mount hpm:raft");
                if (!(vehicle instanceof HpmControllableShip)) throw new AssertionError("Raft is not using Swashbucklers control path");
                double horizontal = Math.hypot(client.player.getX()-vehicle.getX(), client.player.getZ()-vehicle.getZ());
                if (horizontal > 0.25D) throw new AssertionError("Pilot not centered: " + horizontal);
                return vehicle.position();
            });
            context.takeScreenshot("swashbucklers-restored-mounted-pilot");

            context.getInput().holdKeyFor(options -> options.keyUp, 30);
            context.waitTicks(8);
            Vec3 afterW = vehiclePosition(context);
            double moved = horizontalDistance(start, afterW);
            if (moved < 0.50D) throw new AssertionError("Raft did not move under sail: " + moved);

            Vec3 coastStart = afterW;
            context.waitTicks(20);
            Vec3 afterRelease = vehiclePosition(context);
            double coast = horizontalDistance(coastStart, afterRelease);
            if (coast < 0.35D) throw new AssertionError("Ship stopped like vanilla rowing after W release: " + coast);

            float yawBefore = entityYaw(context);
            context.getInput().holdKeyFor(options -> options.keyLeft, 5);
            context.waitTicks(8);
            float yawAfter = entityYaw(context);
            float turn = angleDiff(yawBefore,yawAfter);
            if (turn < 6.0F) throw new AssertionError("Ship did not turn with original steering: " + turn);

            context.getInput().holdKeyFor(options -> options.keyShift, 2);
            context.waitTicks(5);
            context.computeOnClient(client -> {
                if (client.player == null || client.player.getVehicle()!=null) throw new AssertionError("Player did not dismount");
                return true;
            });
            System.out.println("SWASHBUCKLERS_ORIGINAL_CONTROL_OK moved="+moved+" coast="+coast+" turn="+turn);
        }
    }

    private static Vec3 vehiclePosition(ClientGameTestContext context) {
        return context.computeOnClient(client -> {
            if (client.player==null || client.player.getVehicle()==null) throw new AssertionError("Unexpected dismount");
            return client.player.getVehicle().position();
        });
    }

    private static float entityYaw(ClientGameTestContext context) {
        return context.computeOnClient(client -> {
            Entity vehicle = client.player == null ? null : client.player.getVehicle();
            if (!(vehicle instanceof AbstractBoat)) throw new AssertionError("No ship vehicle");
            return vehicle.getYRot();
        });
    }

    private static double horizontalDistance(Vec3 a, Vec3 b) { return Math.hypot(b.x-a.x,b.z-a.z); }
    private static float angleDiff(float a,float b) { float d=Math.abs(a-b)%360.0F; return d>180.0F?360.0F-d:d; }
}
